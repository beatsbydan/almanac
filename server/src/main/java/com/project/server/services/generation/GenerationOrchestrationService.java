package com.project.server.services.generation;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.*;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.scheduleCombination.ScheduleCombinationService;
import com.project.server.db.services.tenantCalendar.TenantCalendarService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.generation.*;
import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationResultResponse;
import com.project.server.dtos.response.generation.UnscheduledCourseResponse;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces manual one-at-a-time schedule entry with a real bulk solve: collects every
 * SchedulableUnit that doesn't have a Schedule yet, asks the generation engine for a conflict-free
 * placement, and persists the result. See Almanac Blueprint §04.
 *
 * One known limitation, called out rather than silently ignored: the data model has no
 * teacher-to-course assignment anywhere — that's always been entered per-Schedule by a human. So
 * the caller must supply it (GenerationTriggerRequest.lecturerAssignments); a course with no
 * assignment is skipped, not guessed at. A second limitation: this assumes no one is concurrently
 * creating a Schedule by hand through the single-schedule flow while a generation run is in
 * progress — the single-schedule path's Redisson locks aren't consulted here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationOrchestrationService {
    private final SchedulableUnitService schedulableUnitService;
    private final ScheduleCombinationService scheduleCombinationService;
    private final ClassRoomService classRoomService;
    private final TenantCalendarService tenantCalendarService;
    private final AppUserService userService;
    private final TimeTableService timeTableService;
    private final ScheduleService scheduleService;
    private final GenerationEngineClient engineClient;

    /** Lets an admin client discover what to put in GenerationTriggerRequest.lecturerAssignments. */
    public List<UnscheduledCourseResponse> getUnscheduledCourses() {
        return getUnscheduledUnits().stream()
                .map(unit -> UnscheduledCourseResponse.builder().code(unit.getCode()).title(unit.getTitle()).build())
                .toList();
    }

    private List<SchedulableUnit> getUnscheduledUnits() {
        return schedulableUnitService.getAll().stream()
                .filter(unit -> !scheduleCombinationService.hasScheduleForCourse(unit.getCode()))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public GenerationResultResponse generate(GenerationTriggerRequest request) throws NotFoundException, ScheduleUpsertException {
        Long tenantId = TenantContext.get();
        TimeTable timeTable = timeTableService.getActiveTimeTable();

        List<ClassRoom> allRooms = classRoomService.getAll();
        List<SchedulableUnit> unscheduled = getUnscheduledUnits();

        List<String> skippedNoTeacher = new ArrayList<>();
        List<EngineSession> sessions = new ArrayList<>();
        Map<String, SchedulableUnit> unitsByCode = new HashMap<>();

        for (SchedulableUnit unit : unscheduled) {
            String lecturerId = request.getLecturerAssignments().get(unit.getCode());
            if (lecturerId == null || lecturerId.isBlank()) {
                skippedNoTeacher.add(unit.getCode());
                continue;
            }

            int expectedSize = unit.getExpectedClassSize() != null ? unit.getExpectedClassSize() : 0;
            int durationHours = estimateDurationHours(unit);

            List<String> eligibleRoomIds = allRooms.stream()
                    .filter(room -> room.getCapacity() >= expectedSize)
                    .map(ClassRoom::getCode)
                    .toList();

            sessions.add(EngineSession.builder()
                    .id(unit.getCode())
                    .teacherId(lecturerId)
                    .durationHours(durationHours)
                    .expectedSize(expectedSize)
                    .eligibleRoomIds(eligibleRoomIds)
                    .participantGroupIds(buildParticipantGroupIds(unit))
                    .build());
            unitsByCode.put(unit.getCode(), unit);
        }

        if (sessions.isEmpty()) {
            return GenerationResultResponse.builder()
                    .scheduledCount(0)
                    .infeasibleUnitCodes(List.of())
                    .skippedNoTeacherUnitCodes(skippedNoTeacher)
                    .message("No unscheduled course with an assigned lecturer was found.")
                    .build();
        }

        EngineGenerationRequest payload = buildEnginePayload(tenantId, allRooms, sessions);
        EngineGenerationResponse response = engineClient.generate(payload);

        if (!"SOLVED".equals(response.getStatus())) {
            return GenerationResultResponse.builder()
                    .scheduledCount(0)
                    .infeasibleUnitCodes(response.getInfeasibleSessionIds())
                    .skippedNoTeacherUnitCodes(skippedNoTeacher)
                    .message("The engine could not find a conflict-free arrangement for every course.")
                    .build();
        }

        int persisted = 0;
        for (EngineAssignment assignment : response.getAssignments()) {
            persistAssignment(assignment, unitsByCode.get(assignment.getSessionId()), timeTable, allRooms,
                    request.getLecturerAssignments().get(assignment.getSessionId()));
            persisted++;
        }

        return GenerationResultResponse.builder()
                .scheduledCount(persisted)
                .infeasibleUnitCodes(List.of())
                .skippedNoTeacherUnitCodes(skippedNoTeacher)
                .message(String.format("Scheduled %d course(s).", persisted))
                .build();
    }

    /**
     * SchedulableUnit has no explicit duration field — it never needed one while a human typed a
     * duration into every ScheduleRequest by hand. Approximated from credit unit, matching the
     * tenant's existing 1/2-hour duration convention, rather than adding a schema column for a
     * value this step could otherwise infer.
     */
    private int estimateDurationHours(SchedulableUnit unit) {
        return unit.getUnit() != null && unit.getUnit() >= 3 ? 2 : 1;
    }

    private List<String> buildParticipantGroupIds(SchedulableUnit unit) {
        List<String> groupIds = new ArrayList<>();
        for (SchedulableUnitParticipant participant : unit.getParticipants()) {
            String level = participant.getLevel() != null ? participant.getLevel().getLevel() : "none";
            for (OrgUnit orgUnit : participant.getOrgUnits()) {
                groupIds.add(String.format("level-%s:dept-%s", level, orgUnit.getShortName()));
            }
        }
        return groupIds;
    }

    private EngineGenerationRequest buildEnginePayload(Long tenantId, List<ClassRoom> allRooms, List<EngineSession> sessions) throws NotFoundException {
        List<EngineRoom> engineRooms = allRooms.stream()
                .map(room -> EngineRoom.builder().id(room.getCode()).capacity(room.getCapacity()).build())
                .toList();

        List<String> daysInUse = tenantCalendarService.getDaysInUse(tenantId);
        List<Integer> periodStartHours = tenantCalendarService.getPeriodTimes(tenantId).stream()
                .map(this::parseStartHour)
                .toList();
        int maxEndHour = tenantCalendarService.getMaxEndHour(tenantId);

        TenantCalendar calendar = tenantCalendarService.getByTenantId(tenantId);
        List<EngineBlackoutWindow> blackoutWindows = calendar.getBlackoutWindows() == null
                ? List.of()
                : calendar.getBlackoutWindows().stream()
                        .map(window -> EngineBlackoutWindow.builder()
                                .day(window.getDay())
                                .startHour(window.getStartTime())
                                .endHour(window.getEndTime())
                                .build())
                        .toList();

        return EngineGenerationRequest.builder()
                .tenantId(String.valueOf(tenantId))
                .days(daysInUse)
                .periodStartHours(periodStartHours)
                .maxEndHour(maxEndHour)
                .blackoutWindows(blackoutWindows)
                .rooms(engineRooms)
                .sessions(sessions)
                .build();
    }

    private int parseStartHour(String periodTime) {
        return Integer.parseInt(periodTime.split(":00")[0]);
    }

    private void persistAssignment(EngineAssignment assignment, SchedulableUnit unit, TimeTable timeTable, List<ClassRoom> allRooms, String lecturerId) throws NotFoundException {
        AppUser staff = userService.getByUserIdOrEmail(lecturerId, null);
        ClassRoom room = allRooms.stream()
                .filter(r -> r.getCode().equals(assignment.getRoomId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Classroom not found."));

        int endHour = assignment.getStartHour() + estimateDurationHours(unit);
        String timePeriod = formatTimePeriod(assignment.getStartHour(), endHour);

        Schedule schedule = Schedule.builder()
                .appUser(staff)
                .timeTable(timeTable)
                .schedulableUnit(unit)
                .classRooms(List.of(room))
                .startTime(assignment.getStartHour())
                .endTime(endHour)
                .timePeriod(timePeriod)
                .day(assignment.getDay())
                .build();
        schedule = scheduleService.save(schedule);

        String combinationKey = String.format("%s:%s:%s:%s", timeTable.getCode(), assignment.getDay(), timePeriod, room.getName());
        ScheduleCombination combination = ScheduleCombination.builder()
                .combination(combinationKey)
                .day(assignment.getDay())
                .startTime(assignment.getStartHour())
                .endTime(endHour)
                .classroomName(room.getName())
                .schedule(schedule)
                .build();
        scheduleCombinationService.save(List.of(combination));
    }

    private String formatTimePeriod(int startHour, int endHour) {
        String startSuffix = startHour >= 12 ? "pm" : "am";
        String endSuffix = endHour >= 12 ? "pm" : "am";
        return String.format("%d:00%s - %d:00%s", startHour, startSuffix, endHour, endSuffix);
    }
}
