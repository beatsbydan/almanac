package com.project.server.services.schedule;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.models.Schedule;
import com.project.server.db.models.TimeTable;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.*;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.ScheduleResponse;
import com.project.server.dtos.response.SuggestionResponse;
import com.project.server.enums.Type;
import com.project.server.services.*;
import com.project.server.services.timetable.TimeTableMgtService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ScheduleMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private ScheduleMgtService scheduleMgtService;
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private TimeTableMgtService timeTableMgtService;
    @Autowired
    private ClassRoomService classRoomService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SchedulableUnitService courseService;
    @Autowired
    private TimeTableService timeTableService;

    List<String> classroomCodes = new ArrayList<>();
    private MessageResponse messageResponse;
    private SchedulableUnit course;


    @BeforeEach
    @SneakyThrows
    void init(){
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        TimeTable timeTable = timeTableService.getActiveTimeTable();
        timeTableMgtService.changeScheduleCreationStatus(timeTable.getCode(), "ENABLED");
        collegeMgtService.createCollege(toCollegeRequest());
        departmentMgtService.createDepartment(toDepartmentRequest());
        authMgtService.createUser(toStaffSignUpRequest());
        authMgtService.createUser(toStudentSignUpRequest());
        classroomMgtService.createClassroom(toClassRoomRequest());
        classroomCodes = classRoomService.findAllByCollege("college").stream().map(ClassRoom::getCode).toList();
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);
        course = courseService.getByTitle(toCourseRequest().getCourseTitle());
        messageResponse = scheduleMgtService.createSchedule(toScheduleRequest(course.getCode(), classroomCodes));
    }

    @Test
    @SneakyThrows
    void should_create_schedule() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_update_schedule() {
        Schedule schedule = scheduleService.getByCourse(toCourseRequest().getCourseTitle());

        messageResponse = scheduleMgtService.updateSchedule(toUpdateScheduleRequest(course.getCode(), classroomCodes), schedule.getCode());
        assertNotNull(messageResponse);

    }

    @Test
    @SneakyThrows
    void should_get_schedules() {
        List<ScheduleResponse> responses = scheduleMgtService.getSchedules("staffId", "Monday" );
        assertNotNull(responses);
    }

    @Test
    @SneakyThrows
    void should_get_suggestions(){
        List<SuggestionResponse> responses = scheduleMgtService.getSuggestions(null, null, null, "college", "< 150");
        assertNotNull(responses);
    }

    @Test
    @SneakyThrows
    void should_get_schedules_by_course_representative() {
        List<ScheduleResponse> responses  = scheduleMgtService.getSchedulesByCourseRepresentative("studentId");
        assertNotNull(responses);
    }

    @Test
    @SneakyThrows
    void should_delete_schedule(){
        Schedule schedule = scheduleService.getByCourse(toCourseRequest().getCourseTitle());
        messageResponse = scheduleMgtService.deleteSchedule(schedule.getCode());
        assertNotNull(messageResponse);
    }

    private ScheduleRequest toUpdateScheduleRequest(String code, List<String> classroomCodes){
        return ScheduleRequest.builder()
                .userId("staffId")
                .courseCode(code)
                .classroomCodes(classroomCodes)
                .recurringDay("Sunday")
                .startTime("8:00am")
                .timeDuration("2")
                .build();
    }

    private ScheduleRequest toScheduleRequest(String code, List<String> classroomCodes){
        return ScheduleRequest.builder()
                .userId("staffId")
                .courseCode(code)
                .classroomCodes(classroomCodes)
                .recurringDay("Sunday")
                .startTime("14:00pm")
                .timeDuration("2")
                .build();
    }

    private CourseRequest toCourseRequest(){
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(List.of("department")).build());
        return CourseRequest.builder()
                .courseTitle("Title")
                .courseUnit("0")
                .general(false)
                .shared(false)
                .batched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("CCC1","CCC2"))
                .expectedClassSize("50")
                .build();
    }

    private ClassroomRequest toClassRoomRequest(){
        return ClassroomRequest.builder()
                .className("classname")
                .classSize(200)
                .college("college")
                .build();
    }

    private StaffSignUpRequest toStaffSignUpRequest(){
        return StaffSignUpRequest.builder()
                .staffEmail("staff@email.com")
                .password("password")
                .staffId("staffId")
                .build();
    }

    private StudentSignUpRequest toStudentSignUpRequest(){
        return StudentSignUpRequest.builder()
                .studentEmail("student@email.com")
                .password("password")
                .studentId("studentId")
                .department("department")
                .level("100")
                .build();
    }

    private DepartmentRequest toDepartmentRequest(){
        return DepartmentRequest.builder()
                .departmentName("department")
                .departmentShortName("departmentShortName")
                .college("college")
                .build();
    }

    private CollegeRequest toCollegeRequest(){
        return CollegeRequest.builder()
                .collegeName("college")
                .collegeShortName("collegeShortName")
                .build();
    }

    private TimetableRequest toTimeTableRequest(){
        return TimetableRequest.builder()
                .session("2002/2003")
                .semester("FIRST")
                .creationStartDate("2003-03-09")
                .creationEndDate("2003-03-12")
                .build();
    }
}