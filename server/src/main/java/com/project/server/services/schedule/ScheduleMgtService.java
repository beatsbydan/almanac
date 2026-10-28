package com.project.server.services.schedule;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.*;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.scheduleCombination.ScheduleCombinationService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.tenantCalendar.TenantCalendarService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.ScheduleRequest;
import com.project.server.dtos.request.course.CourseParticipantResponse;
import com.project.server.dtos.response.*;
import com.project.server.enums.Status;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import com.project.server.services.RedissonLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleMgtService {
    private final AppUserService userService;
    private final ScheduleService scheduleService;
    private final ScheduleCombinationService scheduleCombinationService;
    private final TimeTableService timeTableService;
    private final SchedulableUnitService schedulableUnitService;
    private final ClassRoomService classRoomService;
    private final RedissonLockService redissonLockService;
    private final SuggestionService suggestionService;
    private final TenantCalendarService tenantCalendarService;

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse createSchedule(ScheduleRequest request) throws NotFoundException, DuplicateEntryException, ScheduleUpsertException {
        return validateAndUpsertSchedule(request, null, Type.CREATE);
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse updateSchedule(ScheduleRequest request, String code) throws DuplicateEntryException, ScheduleUpsertException, NotFoundException{
        return validateAndUpsertSchedule(request, code, Type.UPDATE);
    }

    public MessageResponse deleteSchedule(String code) throws NotFoundException{
        scheduleService.getByCode(code);
        scheduleService.deleteByCode(code);
        return new MessageResponse("Schedule deleted.");
    }

    public List<ScheduleResponse> getSchedules(
            String userId,
            String day
    ){
        Optional<TimeTable> timeTable = timeTableService.getActiveTimeTableOptional();
        String timetableCode;

        if(timeTable.isEmpty()) return toResponse(new ArrayList<>());
        else timetableCode = timeTable.get().getCode();

        if(noFilterParams(userId, day)){
            Pageable pageable = PageRequest.of(0, 100);
            return toResponse(scheduleCombinationService.getSchedules(timetableCode, pageable).getContent());
        }

        return toResponse(scheduleCombinationService.getByFilterParams(userId, day, timetableCode));
    }

    public List<ScheduleResponse> getSchedulesByCourseRepresentative(String userId) throws NotFoundException {
        AppUser courseRepresentative = userService.getByUserIdOrEmail(userId, null);
        return toListResponse(scheduleService.getByCourseRepresentative(courseRepresentative));
    }

    public List<SuggestionResponse> getSuggestions(String day, String startTime, String duration, String college, String classSize){
        return suggestionService.getSuggestionsByFilter(day, startTime, duration, college, classSize);
    }

    private boolean noFilterParams(String userId, String day){
        return (userId == null && day == null);
    }

    private MessageResponse validateAndUpsertSchedule(ScheduleRequest request, String scheduleCode, Type type) throws DuplicateEntryException, ScheduleUpsertException, NotFoundException {
        TimeTable timeTable = timeTableService.getActiveTimeTable();
        if(timeTable.getCreationStatus() != Status.ENABLED) throw new ScheduleUpsertException("Schedule creation has not been enabled for this time table.");

        AppUser staff = userService.getByUserIdOrEmail(request.getUserId(), null);
        SchedulableUnit course = schedulableUnitService.getByCodeWithParticipants(request.getCourseCode());

        List<ClassRoom> classRooms = new ArrayList<>();
        for(String classRoomCode : request.getClassroomCodes()){
            classRooms.add(classRoomService.findByCode(classRoomCode));
        }
        List<String> classNames = classRooms.stream().map(ClassRoom::getName).toList();
        int totalClassSize = classRooms.stream().mapToInt(ClassRoom::getCapacity).sum();

        Map<String, Object> timeParams = formatTimePeriod(request);
        int startTime = (Integer) timeParams.get("startTime");
        int endTime = (Integer) timeParams.get("endTime");
        String timePeriod = (String) timeParams.get("timePeriod");

        List<SchedulableUnitParticipant> participants = course.getParticipants();

        List<String> classroomLocks = acquireLocks(classNames, request.getRecurringDay(), timePeriod, timeTable.getCode());
        Set<String> participantLockKeys = buildParticipantLockKeys(participants, request.getRecurringDay(), timePeriod, timeTable.getCode());
        List<String> participantLocks = acquireParticipantLocks(participantLockKeys);
        List<String> courseLock = acquireCourseLock(request.getCourseCode(), timeTable.getCode());

        List<String> allAcquiredLocks = new ArrayList<>(classroomLocks);
        allAcquiredLocks.addAll(participantLocks);
        allAcquiredLocks.addAll(courseLock);

        try {
            if(classroomLocks.size() != classNames.size()) throw new ScheduleUpsertException("This schedule combination is currently unavailable, Try again later.");
            if(participantLocks.size() != participantLockKeys.size()) throw new ScheduleUpsertException("This schedule combination is currently unavailable, Try again later.");
            if(courseLock.isEmpty()) throw new ScheduleUpsertException("This course is currently being scheduled by another request, Try again later.");
            if(tenantCalendarService.hasBlackoutConflict(TenantContext.get(), request.getRecurringDay(), startTime, endTime)) throw new ScheduleUpsertException("Classes aren't supposed to hold at this time on " + request.getRecurringDay() + ".");
            if(course.getExpectedClassSize() > totalClassSize) throw new ScheduleUpsertException("Classroom(s) don't match the expected class size, consider changing venue(s).");

            for(String className : classNames){
                if(scheduleCombinationService.isExistingCombination(
                        request.getRecurringDay(),
                        startTime,
                        endTime,
                        className,
                        timeTable.getCode()
                )) throw new DuplicateEntryException("This schedule combination has been taken by another lecturer. Select another.");
            }

            if(scheduleService.hasScheduleAtTime(startTime, endTime, participants, request.getRecurringDay())){
                log.info("Some participants have schedules at this time");
                boolean isShared = participants.size() > 1 || participants.getFirst().getOrgUnits().size() > 1;
                String message = isShared ? "One or more department(s) taking your course have" : String.format("%s has", participants.getFirst().getOrgUnits().getFirst().getName());

                throw new ScheduleUpsertException(message + " a class at this time.");
            }

            Schedule schedule;
            List<ScheduleCombination> scheduleCombinations = new ArrayList<>();

            if(type == Type.CREATE){
                if(scheduleCombinationService.hasScheduleForCourse(request.getCourseCode())) throw new DuplicateEntryException("Schedule for this course already exists.");
                schedule = scheduleService.save(toCreateScheduleDbModel(staff, timeTable, course, classRooms, request.getRecurringDay(), startTime, endTime, timePeriod));
                for(int i = 0; i < classroomLocks.size(); i++){
                    scheduleCombinations.add(toCreateScheduleCombinationDbModel(classroomLocks.get(i), request.getRecurringDay(), startTime, endTime, classNames.get(i), schedule));
                }
            }
            else {
                schedule = scheduleService.getByCode(scheduleCode);

                schedule.setAppUser(staff);
                schedule.setSchedulableUnit(course);
                schedule.setClassRooms(classRooms);
                schedule.setTimeTable(timeTable);
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setTimePeriod(timePeriod);
                schedule.setDay(request.getRecurringDay());

                schedule = scheduleService.save(schedule);

                scheduleCombinationService.deleteAllBySchedule(scheduleCode);

                for(int i = 0; i < classroomLocks.size(); i++){
                    scheduleCombinations.add(toCreateScheduleCombinationDbModel(classroomLocks.get(i), request.getRecurringDay(), startTime, endTime, classNames.get(i), schedule));
                }
            }

            scheduleCombinationService.save(scheduleCombinations);

            String actionPerformed = type == Type.CREATE ? "created" : "updated";
            return new MessageResponse(String.format("Schedule was successfully %s", actionPerformed));
        } finally {
            releaseLocks(allAcquiredLocks);
        }
    }

    private List<String> acquireLocks(List<String> classrooms, String day, String timePeriod, String timeTableCode){
        List<String> acquiredLocks = new ArrayList<>();

        for(String classroom : classrooms) {
            String lock = String.format("%s:%s:%s:%s", timeTableCode, day, timePeriod, classroom);
            if(redissonLockService.tryLock(lock, 5, 15)) acquiredLocks.add(lock);
        }

        return acquiredLocks;
    }

    /**
     * One lock key per (level, department) pair touched by the course's participants, so two
     * concurrent requests that target different classrooms but the same department/level at the
     * same day and time period are serialized against each other too — closing the gap where only
     * the classroom lock was enforced and {@code hasScheduleAtTime} could be read-then-written by
     * both requests before either committed.
     */
    private Set<String> buildParticipantLockKeys(List<SchedulableUnitParticipant> participants, String day, String timePeriod, String timeTableCode){
        Set<String> keys = new LinkedHashSet<>();

        for(SchedulableUnitParticipant participant : participants){
            for(OrgUnit orgUnit : participant.getOrgUnits()){
                keys.add(String.format("%s:%s:%s:level-%s:dept-%s", timeTableCode, day, timePeriod, participant.getLevel().getLevel(), orgUnit.getShortName()));
            }
        }

        return keys;
    }

    /**
     * The classroom and participant/department locks above are both keyed by (day, timePeriod,
     * ...) — two concurrent requests for the SAME course at DIFFERENT days/times acquire entirely
     * disjoint locks, so neither blocks the other, and both can read hasScheduleForCourse as
     * false before either commits. A course can only ever have one schedule, so that check needs
     * its own lock independent of day/time. Returns a single-element list (or empty, matching the
     * other acquire* methods' shape) so it composes with the existing allAcquiredLocks/finally
     * release pattern.
     */
    private List<String> acquireCourseLock(String courseCode, String timeTableCode){
        String lock = String.format("%s:course:%s", timeTableCode, courseCode);
        return redissonLockService.tryLock(lock, 5, 15) ? List.of(lock) : List.of();
    }

    private List<String> acquireParticipantLocks(Set<String> lockKeys){
        List<String> acquiredLocks = new ArrayList<>();

        for(String key : lockKeys) {
            if(redissonLockService.tryLock(key, 5, 15)) acquiredLocks.add(key);
        }

        return acquiredLocks;
    }

    private void releaseLocks(List<String> locks){
        for (String lock : locks) {
            try {
                redissonLockService.unlock(lock);
            } catch (Exception e) {
                log.warn("Failed to release lock {}: {}", lock, e.getMessage());
            }
        }
    }

    private Map<String, Object> formatTimePeriod(ScheduleRequest request) throws ScheduleUpsertException, NotFoundException{
        Map<String, Object> timeParams = new HashMap<>();

        String splitRegex = ":00";
        List<String> timeSplit = Arrays.stream(request.getStartTime().split(splitRegex)).toList();
        String startTimeString = timeSplit.getFirst();
        String startTimeStringSuffix = timeSplit.getLast();

        int startTime = Integer.parseInt(startTimeString);
        int endTime = startTime + Integer.parseInt(Arrays.stream(request.getTimeDuration().split(" ")).toList().getFirst());

        int maxEndHour = tenantCalendarService.getMaxEndHour(TenantContext.get());
        if(endTime > maxEndHour) throw new ScheduleUpsertException(String.format("Classes can't extend beyond %d:00", maxEndHour));

        String endTimeString = String.valueOf(endTime);
        String endTimeStringSuffix = endTime >= 12 ? "pm" : startTimeStringSuffix;

        String timePeriod =  String.format("%s - %s%s%s", request.getStartTime(), endTimeString, splitRegex, endTimeStringSuffix);

        timeParams.put("startTime", startTime);
        timeParams.put("endTime", endTime);
        timeParams.put("timePeriod", timePeriod);

        return timeParams;
    }

    private Schedule toCreateScheduleDbModel(AppUser staff, TimeTable timetable, SchedulableUnit course, List<ClassRoom> classrooms, String day, int startTime, int endTime, String timePeriod){
        return Schedule.builder()
                .appUser(staff)
                .timeTable(timetable)
                .schedulableUnit(course)
                .classRooms(classrooms)
                .startTime(startTime)
                .endTime(endTime)
                .timePeriod(timePeriod)
                .day(day)
                .build();
    }

    private ScheduleCombination toCreateScheduleCombinationDbModel(String combination, String day, int startTime, int endTime, String classroom, Schedule schedule){
        return ScheduleCombination.builder()
                .combination(combination)
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .classroomName(classroom)
                .schedule(schedule)
                .build();
    }

    private ScheduleResponse toScheduleResponse(Schedule schedule){
        return ScheduleResponse.builder()
                .classrooms(toClassroomResponse(schedule.getClassRooms()))
                .course(toCourseResponse(schedule.getSchedulableUnit()))
                .day(schedule.getDay())
                .code(schedule.getCode())
                .time(schedule.getTimePeriod())
                .build();
    }


    private CourseResponse toCourseResponse(SchedulableUnit course) {
        List<String> courseCodesResponse = new ArrayList<>();

        List<SchedulableUnitParticipant> courseParticipants = schedulableUnitService.getParticipants(course.getCode());
        List<CourseParticipantResponse> courseParticipantResponses = new ArrayList<>();

        for(SchedulableUnitParticipant participant : courseParticipants) {
            courseParticipantResponses.add(CourseParticipantResponse.builder()
                    .level(participant.getLevel().getLevel())
                    .departments(participant.getOrgUnits().stream().map(OrgUnit::getName).toList())
                    .build());
        }

        for (SchedulableUnitCode code : course.getCodes()) {
            courseCodesResponse.add(code.getUnitCode());
        }

        return CourseResponse.builder()
                 .code(course.getCode())
                 .courseTitle(course.getTitle())
                 .courseUnit(course.getUnit().toString())
                 .batched(course.isBatched())
                 .shared(course.isShared())
                 .general(course.isGeneral())
                 .expectedClassSize(course.getExpectedClassSize().toString())
                 .courseCodes(courseCodesResponse)
                 .courseParticipants(courseParticipantResponses)
                 .build();
    }

    private List<ClassroomResponse> toClassroomResponse(List<ClassRoom> classRooms){
        List<ClassroomResponse> responses = new ArrayList<>();

        for(ClassRoom room : classRooms){
            String college = room.getOrgUnit() != null ? room.getOrgUnit().getShortName() : null;
            responses.add(ClassroomResponse.builder()
                    .className(room.getName())
                    .code(room.getCode())
                    .classSize(room.getCapacity())
                    .college(college)
                    .build());
        }

        return responses;
    }

    private List<ScheduleResponse> toResponse(List<ScheduleCombination> scheduleCombinations){
        List<ScheduleResponse> responses = new ArrayList<>();

        Map<String, Schedule> schedulePerCombination = new HashMap<>();

        for(ScheduleCombination combination : scheduleCombinations){
            Schedule combinationSchedule = combination.getSchedule();
            if(!schedulePerCombination.containsKey(combinationSchedule.getCode())){
                schedulePerCombination.put(combinationSchedule.getCode(), combinationSchedule);
            }
        }

        List<Schedule> schedules = schedulePerCombination.values().stream().toList();
        for(Schedule schedule : schedules){
            responses.add(toScheduleResponse(schedule));
        }

        return responses;
    }

    private List<ScheduleResponse> toListResponse (List<Schedule> schedules){
        List<ScheduleResponse> responses = new ArrayList<>();

        for(Schedule schedule : schedules){
            responses.add(toScheduleResponse(schedule));
        }

        return responses;
    }
}