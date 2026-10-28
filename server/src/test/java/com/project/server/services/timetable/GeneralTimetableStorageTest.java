package com.project.server.services.timetable;

import com.project.server.BaseIntegrationTest;
import com.project.server.cron.services.TimeTableGenerationCronService;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.request.ScheduleRequest;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.enums.Status;
import com.project.server.enums.Type;
import com.project.server.services.*;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.timetable.TimeTableService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the cron's write of the completed general timetable actually ends up retrievable
 * through DocumentStorage, not just on local disk — the point of the §06 adapter integration.
 * Uses the UWC (university-wide, college-less) generation path so the fixture doesn't need to
 * match one of the hardcoded Bowen college-name GenerationStage values.
 */
class GeneralTimetableStorageTest extends BaseIntegrationTest {
    @Autowired
    private TimeTableMgtService timeTableMgtService;
    @Autowired
    private TimeTableGenerationCronService cronService;
    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private ClassRoomService classRoomService;
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private SchedulableUnitService schedulableUnitService;
    @Autowired
    private com.project.server.services.schedule.ScheduleMgtService scheduleMgtService;

    @Test
    @SneakyThrows
    void cronWrittenDocument_isRetrievableThroughDocumentStorage() {
        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest());
        var timeTable = timeTableService.getActiveTimeTable();
        timeTableMgtService.changeScheduleCreationStatus(timeTable.getCode(), "ENABLED");

        // a classroom with no college, and a "general" (university-wide) course, is exactly the
        // UWC stage's criteria — no Bowen-specific college short name needed.
        classroomMgtService.createClassroom(toClassRoomRequest());
        authMgtService.createUser(toStaffSignUpRequest());
        courseMgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);

        var course = schedulableUnitService.getByTitle("General Course");
        var classroomCodes = classRoomService.getClassroomsWithoutCollege().stream().map(c -> c.getCode()).toList();
        assertFalse(classroomCodes.isEmpty());

        MessageResponse scheduleResponse = scheduleMgtService.createSchedule(toScheduleRequest(course.getCode(), classroomCodes));
        assertNotNull(scheduleResponse);

        timeTableMgtService.changeScheduleCreationStatus(timeTable.getCode(), Status.PAUSED.name());
        timeTableMgtService.initiateGeneration();

        cronService.generate();

        DocumentResponse downloaded = timeTableMgtService.downloadGeneralTimeTable();

        assertNotNull(downloaded);
        assertNotNull(downloaded.getBytesArr());
        assertTrue(downloaded.getBytesArr().length > 0, "the downloaded document should have real content");
        assertEquals(timeTable.getFileName(), downloaded.getFileName());
    }

    private TimetableRequest toTimeTableRequest() {
        return TimetableRequest.builder()
                .session("2010/2011")
                .semester("FIRST")
                .creationStartDate("2010-03-09")
                .creationEndDate("2010-03-12")
                .build();
    }

    private StaffSignUpRequest toStaffSignUpRequest() {
        return StaffSignUpRequest.builder()
                .staffEmail("storage-staff@email.com")
                .password("password")
                .staffId("storageStaffId")
                .build();
    }

    private ClassroomRequest toClassRoomRequest() {
        return ClassroomRequest.builder()
                .className("storage-test-room")
                .classSize(200)
                .build();
    }

    private CourseRequest toCourseRequest() {
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        return CourseRequest.builder()
                .courseTitle("General Course")
                .courseUnit("2")
                .general(true)
                .shared(false)
                .batched(false)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("GEN101"))
                .expectedClassSize("50")
                .build();
    }

    private ScheduleRequest toScheduleRequest(String courseCode, List<String> classroomCodes) {
        return ScheduleRequest.builder()
                .userId("storageStaffId")
                .courseCode(courseCode)
                .classroomCodes(classroomCodes)
                .recurringDay("Mondays")
                .startTime("8:00am")
                .timeDuration("1")
                .build();
    }
}
