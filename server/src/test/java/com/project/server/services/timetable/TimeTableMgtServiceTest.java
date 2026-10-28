package com.project.server.services.timetable;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.models.TimeTable;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.*;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.TimeTableResponse;
import com.project.server.enums.Status;
import com.project.server.enums.Type;
import com.project.server.services.*;
import com.project.server.services.schedule.ScheduleMgtService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeTableMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private TimeTableMgtService mgtService;
    @Autowired
    public ScheduleMgtService scheduleMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private ClassRoomService classRoomService;
    @Autowired
    private SchedulableUnitService courseService;

    List<String> classroomCodes = new ArrayList<>();
    private MessageResponse messageResponse;
    private TimeTable timeTable;

    @BeforeEach
    @SneakyThrows
    void init(){
        mgtService.deactivate();
        messageResponse = mgtService.createTimeTable(toTimeTableRequest());
        timeTable = timeTableService.getActiveTimeTable();
        mgtService.changeScheduleCreationStatus(timeTable.getCode(), "ENABLED");
        departmentMgtService.createDepartment(toDepartmentRequest());
        collegeMgtService.createCollege(toCollegeRequest());
        authMgtService.createUser(toUserSignUpRequest());
        classroomMgtService.createClassroom(toClassRoomRequest());
        classroomCodes = classRoomService.findAllByCollege("college").stream().map(ClassRoom::getCode).toList();
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);
        SchedulableUnit course = courseService.getByTitle(toCourseRequest().getCourseTitle());
        scheduleMgtService.createSchedule(toScheduleRequest(course.getCode(), classroomCodes));
    }

    @Test
    @SneakyThrows
    void createTimeTable() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void updateTimeTable() {
        timeTable = timeTableService.getActiveTimeTable();
        messageResponse = mgtService.updateTimeTable(timeTable.getCode(), toTimeTableRequest());

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void changeScheduleCreationStatus() {
        timeTable = timeTableService.getActiveTimeTable();
    }

    @Test
    @SneakyThrows
    void should_initiate_generation(){
        mgtService.changeScheduleCreationStatus(timeTable.getCode(), Status.PAUSED.name());
        messageResponse = mgtService.initiateGeneration();
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void getActiveTimeTable() {
        TimeTableResponse response = mgtService.getActiveTimeTable();
        assertNotNull(response);
    }

    @Test
    @SneakyThrows
    void generateTimeTable() {
        timeTable = timeTableService.getActiveTimeTable();
        DocumentResponse response = mgtService.generateAndDownloadUserTimeTable(timeTable.getCode(), toUserSignUpRequest().getStaffId());

        assertNotNull(response);
    }

    @Test
    @SneakyThrows
    void deactivate(){
        messageResponse = mgtService.deactivate();

        assertNotNull(messageResponse);
    }

    private TimetableRequest toTimeTableRequest(){
        return TimetableRequest.builder()
                .session("2002/2003")
                .semester("FIRST")
                .creationStartDate("2003-03-09")
                .creationEndDate("2003-03-12")
                .build();
    }

    private StaffSignUpRequest toUserSignUpRequest(){
        return StaffSignUpRequest.builder()
                .staffEmail("email@email.com")
                .password("password")
                .staffId("staffId")
                .build();
    }


    private DepartmentRequest toDepartmentRequest(){
        return DepartmentRequest.builder()
                .departmentName("department")
                .departmentShortName("departmentShortName")
                .build();
    }

    private CollegeRequest toCollegeRequest(){
        return CollegeRequest.builder()
                .collegeName("college")
                .collegeShortName("collegeShortName")
                .build();
    }
    private ScheduleRequest toScheduleRequest(String code, List<String> classroomCodes){
        return ScheduleRequest.builder()
                .userId("staffId")
                .courseCode(code)
                .classroomCodes(classroomCodes)
                .recurringDay("Monday")
                .startTime("8:00am")
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
}