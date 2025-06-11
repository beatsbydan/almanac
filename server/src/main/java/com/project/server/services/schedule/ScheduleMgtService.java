package com.project.server.services.schedule;

import com.project.server.db.models.*;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.course.CourseService;
import com.project.server.db.services.courseParticipant.CourseParticipantService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.scheduleCombination.ScheduleCombinationService;
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
    private final CourseService courseService;
    private final ClassRoomService classRoomService;
    private final RedissonLockService redissonLockService;
    private final SuggestionService suggestionService;
    private final CourseParticipantService courseParticipantService;

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
        /*
        * TODO: Optional but we can implement an algorithm to check if there's a schedule for a particular department before the new schedule to be created, the location of the classroom for the class and check it against the location for the new schedule to see if they're both on the same side or not. Notify users that a 15 minutes grace would be given so the class would be for 1hr:45mins to enable students get to the venue in time. This factors students into the system.
        */

        TimeTable timeTable = timeTableService.getActiveTimeTable();
        if(timeTable.getCreationStatus() != Status.ENABLED) throw new ScheduleUpsertException("Schedule creation has not been enabled for this time table.");

        AppUser staff = userService.getByUserIdOrEmail(request.getUserId(), null);
        Course course = courseService.findByCode(request.getCourseCode());

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

        List<String> locks = acquireLocks(classNames, request.getRecurringDay(), timePeriod, timeTable.getCode());
        if(locks.size() != classNames.size()) throw new ScheduleUpsertException("This schedule combination is currently unavailable, Try again later.");

        for(int i = 0; i < locks.size(); i++){
            if(scheduleCombinationService.isExistingCombination(
                    request.getRecurringDay(),
                    startTime,
                    endTime,
                    classNames.get(i),
                    timeTable.getCode()
            )) throw new DuplicateEntryException("This schedule combination has been taken by another lecturer. Select another.");
        }

        if(Objects.equals(request.getRecurringDay(), "Wednesdays") && (startTime >= 10 && startTime <=12)) throw new ScheduleUpsertException("Classes aren't supposed to hold at this time on Wednesdays.");
        if(course.getExpectedClassSize() > totalClassSize) throw new ScheduleUpsertException("Classroom(s) don't match the expected class size, consider changing venue(s).");

        List<CourseParticipant> participants = course.getCourseParticipants();
        if(scheduleService.hasScheduleAtTime(startTime, endTime, participants)){
            boolean isShared = participants.size() > 1 || participants.getFirst().getDepartments().size() > 1;
            String message = isShared ? "One or more department(s) taking your course have" : String.format("%s has", participants.getFirst().getDepartments().getFirst().getName());

            throw new ScheduleUpsertException(message + " a class at this time.");
        }

        Schedule schedule;
        List<ScheduleCombination> scheduleCombinations = new ArrayList<>();

        if(type == Type.CREATE){
            if(scheduleCombinationService.hasScheduleForCourse(request.getCourseCode())) throw new DuplicateEntryException("Schedule for this course already exists.");
            schedule = scheduleService.save(toCreateScheduleDbModel(staff, timeTable, course, classRooms, request.getRecurringDay(), startTime, endTime, timePeriod));
            for(int i = 0; i < locks.size(); i++){
                scheduleCombinations.add(toCreateScheduleCombinationDbModel(locks.get(i), request.getRecurringDay(), startTime, endTime, classNames.get(i), schedule));
            }
        }
        else {
            schedule = scheduleService.getByCode(scheduleCode);

            schedule.setAppUser(staff);
            schedule.setCourse(course);
            schedule.setClassRooms(classRooms);
            schedule.setTimeTable(timeTable);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setTimePeriod(timePeriod);
            schedule.setDay(request.getRecurringDay());

            schedule = scheduleService.save(schedule);

            scheduleCombinationService.deleteAllBySchedule(scheduleCode);

            for(int i = 0; i < locks.size(); i++){
                scheduleCombinations.add(toCreateScheduleCombinationDbModel(locks.get(i), request.getRecurringDay(), startTime, endTime,classNames.get(i), schedule));
            }
        }

        scheduleCombinationService.save(scheduleCombinations);

        for (String lock : locks) {
            log.info("Unlocking lock: {}", lock);
            redissonLockService.unlock(lock);
        }

        String actionPerformed = type == Type.CREATE ? "created" : "updated";
        return new MessageResponse(String.format("Schedule was successfully %s", actionPerformed));
    }

    private List<String> acquireLocks(List<String> classrooms, String day, String timePeriod, String timeTableCode){
        List<String> acquiredLocks = new ArrayList<>();

        for(String classroom : classrooms) {
            String lock = String.format("%s:%s:%s:%s", timeTableCode, day, timePeriod, classroom);
            if(redissonLockService.tryLock(lock, 5, 15)) acquiredLocks.add(lock);
        }

        return acquiredLocks;
    }

    private Map<String, Object> formatTimePeriod(ScheduleRequest request) throws ScheduleUpsertException{
        Map<String, Object> timeParams = new HashMap<>();

        String splitRegex = ":00";
        List<String> timeSplit = Arrays.stream(request.getStartTime().split(splitRegex)).toList();
        String startTimeString = timeSplit.getFirst();
        String startTimeStringSuffix = timeSplit.getLast();

        int startTime = Integer.parseInt(startTimeString);
        int endTime = startTime + Integer.parseInt(Arrays.stream(request.getTimeDuration().split(" ")).toList().getFirst());

        if(endTime > 18) throw new ScheduleUpsertException("Classes can't extend beyond 18:00PM");

        String endTimeString = String.valueOf(endTime);
        String endTimeStringSuffix = endTime >= 12 ? "pm" : startTimeStringSuffix;

        String timePeriod =  String.format("%s - %s%s%s", request.getStartTime(), endTimeString, splitRegex, endTimeStringSuffix);

        timeParams.put("startTime", startTime);
        timeParams.put("endTime", endTime);
        timeParams.put("timePeriod", timePeriod);

        return timeParams;
    }

    private Schedule toCreateScheduleDbModel(AppUser staff, TimeTable timetable, Course course, List<ClassRoom> classrooms, String day, int startTime, int endTime, String timePeriod){
        return Schedule.builder()
                .appUser(staff)
                .timeTable(timetable)
                .course(course)
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
                .course(toCourseResponse(schedule.getCourse()))
                .day(schedule.getDay())
                .code(schedule.getCode())
                .time(schedule.getTimePeriod())
                .build();
    }


    private CourseResponse toCourseResponse(Course course) {
        List<String> courseCodesResponse = new ArrayList<>();

        List<CourseParticipant> courseParticipants = courseParticipantService.getByCourse(course.getCode());
        List<CourseParticipantResponse> courseParticipantResponses = new ArrayList<>();

        for(CourseParticipant participant : courseParticipants) {
            courseParticipantResponses.add(CourseParticipantResponse.builder()
                    .level(participant.getLevel().getLevel())
                    .departments(participant.getDepartments().stream().map(Department::getName).toList())
                    .build());
        }

        for (CourseCode code : course.getCourseCodes()) {
            courseCodesResponse.add(code.getCourseCode());
        }

        return CourseResponse.builder()
                 .code(course.getCode())
                 .courseTitle(course.getCourseTitle())
                 .courseUnit(course.getCourseUnit().toString())
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
            String college = room.getCollege() != null ? room.getCollege().getShortName() : null;
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