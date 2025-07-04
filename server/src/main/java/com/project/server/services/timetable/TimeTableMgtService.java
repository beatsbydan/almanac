package com.project.server.services.timetable;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.GenerationTracker;
import com.project.server.db.models.TimeTable;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.generationTracker.GenerationTrackerService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.TimeTableResponse;
import com.project.server.enums.Semester;
import com.project.server.enums.Status;
import com.project.server.exceptions.*;
import com.project.server.util.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static com.project.server.util.VariableUtil.TIME_TABLE_DIRECTORY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableMgtService {
    private final TimeTableService timeTableService;
    private final TimeTableGenerationService timeTableGenerationService;
    private final AppUserService userService;
    private final GenerationTrackerService generationTrackerService;

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

    public MessageResponse initiateGeneration() throws ScheduleUpsertException {
        TimeTable timeTable = timeTableService.getActiveTimeTable();
        if(timeTable.getCreationStatus().equals(Status.ENABLED)) throw new ScheduleUpsertException("Timetable creation is still in progress..");
        timeTable.setStatus(Status.READY_FOR_GENERATION);
        timeTable.setGenerationTracker(generationTrackerService.save(toGenerationTrackerDbModel()));

        timeTableService.save(timeTable);

        return new MessageResponse("TimeTable Generation has begun!");
    }

    public MessageResponse deactivate() {
        TimeTable activeTimeTable = timeTableService.getActiveTimeTableOptional().orElse(null);

        if(activeTimeTable != null) activeTimeTable.setActive(false);
        else return new MessageResponse("No active timetable is present");

        timeTableService.save(activeTimeTable);

        return new MessageResponse("TimeTable was deactivated successfully");
    }

    public DocumentResponse generateAndDownloadUserTimeTable(String code, String userId) throws NotFoundException, TimeTableGenerationException {
        TimeTable timeTable = timeTableService.findByCode(code);
        AppUser user = userService.getByUserIdOrEmail(userId, null);

        return timeTableGenerationService.generate(timeTable, user);
    }

    public FileSystemResource downloadGeneralTimeTable() throws ScheduleUpsertException, BadRequestException, NotFoundException {
        TimeTable timeTable = timeTableService.getActiveTimeTable();
        Status status = timeTable.getStatus();
        if(status.equals(Status.PENDING_GENERATION)) throw new BadRequestException("Generation has not been initiated for this timetable");
        else if(status.equals(Status.READY_FOR_GENERATION)) throw new BadRequestException("Generation is in progress, please check back later..");
        else if(status.equals(Status.FAILED_TO_GENERATE)) throw new BadRequestException("Generation failed, Please retry..");

        File file = new File(TIME_TABLE_DIRECTORY, timeTable.getFileName());
        if(!file.exists()){
            throw new NotFoundException("Timetable -> " + timeTable.getFileName() + " is unavailable at this time.");
        }

        return new FileSystemResource(file);
    }

    private TimeTable toCreateDbModel(TimetableRequest request){
        String session = String.join("-", request.getSession().split("/"));
        return TimeTable.builder()
                .session(session)
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

    private GenerationTracker toGenerationTrackerDbModel(){
        return GenerationTracker.builder().build();
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