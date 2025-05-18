package com.project.server.services.timetable;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.Schedule;
import com.project.server.db.models.TimeTable;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.TimeTableResponse;
import com.project.server.enums.Role;
import com.project.server.enums.Semester;
import com.project.server.enums.Status;
import com.project.server.exceptions.*;
import com.project.server.util.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableMgtService {
    private final TimeTableService timeTableService;
    private final TimeTableGenerationService timeTableGenerationService;
    private final AppUserService userService;

    public MessageResponse createTimeTable(TimetableRequest request) throws DuplicateEntryException, BadRequestException {
        if(timeTableService.activeTimeTableExists()) throw new DuplicateEntryException("There's already an active timetable.");
        else if(timeTableService.existsBySessionAndSemester(request)) throw new BadRequestException("There's a timetable for this session and semester.");

        timeTableService.save(toCreateDbModel(request));

        return new MessageResponse("Timetable was successfully created.");
    }
    public MessageResponse updateTimeTable(String code, TimetableRequest request) throws NotFoundException {
        TimeTable timeTable = timeTableService.findByCode(code);

        timeTableService.save(toUpdateDbModel(timeTable, request));

        return new MessageResponse("Timetable was successfully updated.");
    }

    public MessageResponse changeScheduleCreationStatus(String code, String status) throws NotFoundException{
        TimeTable timeTable = timeTableService.findByCode(code);

        timeTable.setCreationStatus(Status.valueOf(status));
        timeTableService.save(timeTable);

        String action = Objects.equals(status, Status.ENABLED.name()) ? "ENABLED" : "DISABLED";
        return new MessageResponse(String.format("Schedule creation has been %s.", action));
    }

    public TimeTableResponse getActiveTimeTable(){
        Optional<TimeTable> timeTableOptional = timeTableService.getActiveTimeTableOptional();
        TimeTable timeTable;

        if(timeTableOptional.isPresent()){
            timeTable = timeTableOptional.get();
            return toTimeTableResponse(timeTable);
        }

        return null;
    }

    public MessageResponse deactivate() {
        TimeTable activeTimeTable = timeTableService.getActiveTimeTableOptional().orElse(null);

        if(activeTimeTable != null) activeTimeTable.setActive(false);
        else return new MessageResponse("No active timetable is present");

        timeTableService.save(activeTimeTable);

        return new MessageResponse("TimeTable was deactivated successfully");
    }

    public DocumentResponse generateTimeTable(String code, String userId) throws NotFoundException, TimeTableGenerationException {
        TimeTable timeTable = timeTableService.findByCode(code);
        AppUser user = userService.getByUserIdOrEmail(userId, null);

        // MAYBE: For shared courses, develop an algorithm to split the classrooms effectively amongst departments and allocate

        return timeTableGenerationService.generate(timeTable, user);
    }

    private TimeTable toCreateDbModel(TimetableRequest request){
        return TimeTable.builder()
                .session(request.getSession())
                .creationStartDate(HelperUtil.parseDate(request.getCreationStartDate()))
                .creationEndDate(HelperUtil.parseDate(request.getCreationEndDate()))
                .fileName(HelperUtil.generateFilename(request.getSession(), request.getSemester()))
                .semester(Semester.valueOf(request.getSemester()))
                .build();
    }

    private TimeTable toUpdateDbModel(TimeTable timeTable, TimetableRequest request){
        timeTable.setCreationStartDate(HelperUtil.parseDate(request.getCreationStartDate()));
        timeTable.setCreationEndDate(HelperUtil.parseDate(request.getCreationEndDate()));
        timeTable.setSession(request.getSession());

        return timeTable;
    }

    private TimeTableResponse toTimeTableResponse(TimeTable timeTable){
        return TimeTableResponse.builder()
                .code(timeTable.getCode())
                .fileName(timeTable.getFileName())
                .session(timeTable.getSession())
                .active(timeTable.isActive())
                .status(timeTable.getStatus().name())
                .semester(timeTable.getSemester().name())
                .canCreateSchedules(timeTable.getCreationStatus() == Status.ENABLED)
                .creationEndDate(timeTable.getCreationEndDate())
                .creationStartDate(timeTable.getCreationStartDate())
                .build();
    }
}