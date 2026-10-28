package com.project.server.services.timetable;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.GenerationTracker;
import com.project.server.db.models.TimeTable;
import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.generationTracker.GenerationTrackerService;
import com.project.server.db.services.tenant.TenantService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.TimeTableResponse;
import com.project.server.enums.Semester;
import com.project.server.enums.Status;
import com.project.server.exceptions.*;
import com.project.server.services.storage.DocumentStorage;
import com.project.server.util.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableMgtService {
    private final TimeTableService timeTableService;
    private final TimeTableGenerationService timeTableGenerationService;
    private final AppUserService userService;
    private final GenerationTrackerService generationTrackerService;
    private final DocumentStorage documentStorage;
    private final TenantService tenantService;

    public MessageResponse createTimeTable(TimetableRequest request) throws DuplicateEntryException, BadRequestException, NotFoundException {
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

    public DocumentResponse downloadGeneralTimeTable() throws ScheduleUpsertException, BadRequestException, NotFoundException {
        TimeTable timeTable = timeTableService.getActiveTimeTable();
        Status status = timeTable.getStatus();
        if(status.equals(Status.PENDING_GENERATION)) throw new BadRequestException("Generation has not been initiated for this timetable");
        else if(status.equals(Status.READY_FOR_GENERATION)) throw new BadRequestException("Generation is in progress, please check back later..");
        else if(status.equals(Status.FAILED_TO_GENERATE)) throw new BadRequestException("Generation failed, Please retry..");

        byte[] content = documentStorage.retrieve(TenantContext.get(), timeTable.getFileName());
        return new DocumentResponse(content, timeTable.getFileName());
    }

    private TimeTable toCreateDbModel(TimetableRequest request) throws NotFoundException {
        String session = String.join("-", request.getSession().split("/"));
        String tenantName = tenantService.getById(TenantContext.get()).getName();
        return TimeTable.builder()
                .session(session)
                .creationStartDate(HelperUtil.parseDate(request.getCreationStartDate()))
                .creationEndDate(HelperUtil.parseDate(request.getCreationEndDate()))
                .fileName(HelperUtil.generateFilename(tenantName, session, request.getSemester()))
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