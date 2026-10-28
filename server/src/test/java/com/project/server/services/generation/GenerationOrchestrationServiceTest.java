package com.project.server.services.generation;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.GenerationJob;
import com.project.server.db.services.generationJob.GenerationJobService;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationJobResponse;
import com.project.server.dtos.response.generation.GenerationResultResponse;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.enums.GenerationJobStatus;
import com.project.server.enums.Type;
import com.project.server.services.*;
import com.project.server.services.timetable.TimeTableMgtService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real cross-process integration test: requires the Python generation engine (engine/) running
 * locally on port 8001 — started manually before running this class, not managed by Maven. This
 * is the one test that proves the Java orchestration and the Python solver actually agree on the
 * wire contract, not just that each compiles against its own mock of the other.
 */
@SpringBootTest(properties = "project.generation-engine.url=http://localhost:8001")
class GenerationOrchestrationServiceTest extends BaseIntegrationTest {
    @Autowired
    private GenerationOrchestrationService generationOrchestrationService;
    @Autowired
    private TimeTableMgtService timeTableMgtService;
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private com.project.server.db.services.timetable.TimeTableService timeTableService;
    @Autowired
    private GenerationWorker generationWorker;
    @Autowired
    private GenerationJobService generationJobService;

    @Test
    @SneakyThrows
    void generate_schedulesAnUnscheduledCourse_viaTheRealEngine() {
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        var timeTable = timeTableService.getActiveTimeTable();
        timeTableMgtService.changeScheduleCreationStatus(timeTable.getCode(), "ENABLED");

        collegeMgtService.createCollege(toCollegeRequest());
        departmentMgtService.createDepartment(toDepartmentRequest());
        authMgtService.createUser(toStaffSignUpRequest());
        classroomMgtService.createClassroom(toClassRoomRequest());
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);

        // the course code is generated server-side; resolve it the same way an admin client
        // would have to — by listing courses and matching on title.
        var courses = courseMgtService.filterCourses("Title");
        assertFalse(courses.isEmpty());
        String courseCode = courses.getFirst().getCode();

        GenerationTriggerRequest request = GenerationTriggerRequest.builder()
                .lecturerAssignments(Map.of(courseCode, "staffId"))
                .build();

        GenerationResultResponse result = generationOrchestrationService.generate(request);

        assertEquals(1, result.getScheduledCount());
        assertTrue(result.getInfeasibleUnitCodes().isEmpty());
        assertTrue(result.getSkippedNoTeacherUnitCodes().isEmpty());
    }

    @Test
    @SneakyThrows
    void getUnscheduledCourses_listsACreatedCourseWithNoScheduleYet() {
        // doesn't call the engine at all — exercises only the read-side listing, so it works
        // whether or not the engine process happens to be running.
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        timeTableMgtService.changeScheduleCreationStatus(timeTableService.getActiveTimeTable().getCode(), "ENABLED");

        collegeMgtService.createCollege(toCollegeRequest());
        departmentMgtService.createDepartment(toDepartmentRequest());
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);

        var unscheduled = generationOrchestrationService.getUnscheduledCourses();

        assertEquals(1, unscheduled.size());
        assertEquals("Title", unscheduled.getFirst().getTitle());
    }

    @Test
    @SneakyThrows
    void trigger_createsAPendingJobImmediatelyWithoutWaitingForTheEngine() {
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        timeTableMgtService.changeScheduleCreationStatus(timeTableService.getActiveTimeTable().getCode(), "ENABLED");
        collegeMgtService.createCollege(toCollegeRequest());
        departmentMgtService.createDepartment(toDepartmentRequest());
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);
        String courseCode = courseMgtService.filterCourses("Title").getFirst().getCode();

        GenerationJobResponse response = generationOrchestrationService.trigger(
                GenerationTriggerRequest.builder().lecturerAssignments(Map.of(courseCode, "staffId")).build());

        assertNotNull(response.getCode());
        assertEquals(GenerationJobStatus.PENDING, response.getStatus());
    }

    /**
     * runSync is GenerationWorker's actual logic, called directly (same thread, same transaction)
     * rather than through its @Async entry point — avoids racing a background thread against this
     * test's transaction-rollback-based cleanup. The engine genuinely isn't running in this
     * environment, so this exercises the real failure-handling path, not a mocked one.
     */
    @Test
    @SneakyThrows
    void runSync_marksTheJobFailedWhenTheEngineIsUnreachable() {
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        timeTableMgtService.changeScheduleCreationStatus(timeTableService.getActiveTimeTable().getCode(), "ENABLED");
        collegeMgtService.createCollege(toCollegeRequest());
        departmentMgtService.createDepartment(toDepartmentRequest());
        authMgtService.createUser(toStaffSignUpRequest());
        classroomMgtService.createClassroom(toClassRoomRequest());
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);
        String courseCode = courseMgtService.filterCourses("Title").getFirst().getCode();

        GenerationJob job = generationJobService.save(GenerationJob.builder().status(GenerationJobStatus.PENDING).build());
        GenerationTriggerRequest request = GenerationTriggerRequest.builder()
                .lecturerAssignments(Map.of(courseCode, "staffId"))
                .build();

        generationWorker.runSync(job.getCode(), request);

        GenerationJob updated = generationJobService.getByCode(job.getCode());
        assertEquals(GenerationJobStatus.FAILED, updated.getStatus());
        assertNotNull(updated.getMessage());
    }

    private TimetableRequest toTimeTableRequest() {
        return TimetableRequest.builder()
                .session("2002/2003")
                .semester("FIRST")
                .creationStartDate("2003-03-09")
                .creationEndDate("2003-03-12")
                .build();
    }

    private StaffSignUpRequest toStaffSignUpRequest() {
        return StaffSignUpRequest.builder()
                .staffEmail("staff@email.com")
                .password("password")
                .staffId("staffId")
                .build();
    }

    private DepartmentRequest toDepartmentRequest() {
        return DepartmentRequest.builder()
                .departmentName("department")
                .departmentShortName("departmentShortName")
                .college("college")
                .build();
    }

    private CollegeRequest toCollegeRequest() {
        return CollegeRequest.builder()
                .collegeName("college")
                .collegeShortName("collegeShortName")
                .build();
    }

    private ClassroomRequest toClassRoomRequest() {
        return ClassroomRequest.builder()
                .className("classname")
                .classSize(200)
                .college("college")
                .build();
    }

    private CourseRequest toCourseRequest() {
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(List.of("department")).build());
        return CourseRequest.builder()
                .courseTitle("Title")
                .courseUnit("3")
                .general(false)
                .shared(false)
                .batched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("CCC1"))
                .expectedClassSize("50")
                .build();
    }
}
