package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.CourseResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.enums.Type;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private CourseMgtService mgtService;
    @Autowired
    private SchedulableUnitService courseService;
    @Autowired
    private OrgUnitService orgUnitService;
    private MessageResponse messageResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        OrgUnit college = orgUnitService.save(OrgUnit.builder().name("collegeName").shortName("collegeShortName").type("College").build());
        orgUnitService.save(OrgUnit.builder().name("department").shortName("DEPT").type("Department").parent(college).build());
        messageResponse = mgtService.upsertCourse(toCourseRequest(), Type.CREATE, null);
    }

    @Test
    @SneakyThrows
    void should_create_course() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_update_course() {
        SchedulableUnit course = courseService.getByTitle(toCourseRequest().getCourseTitle());
        assertNotNull(mgtService.upsertCourse(toCourseRequest() , Type.UPDATE, course.getCode()));
    }

    @Test
    @SneakyThrows
    void should_get_courses() {
        List<CourseResponse> response = mgtService.getCourses(
                toCourseRequest().getCourseUnit(),
                toCourseRequest().getCourseParticipants().getFirst().getDepartments().getFirst(),
                null,
                toCourseRequest().isShared(),
                toCourseRequest().isBatched(),
                toCourseRequest().isGeneral()

        );

        assertNotNull(response);
    }

    @Test
    @SneakyThrows
    void should_filter_courses() {
        List<CourseResponse> response = mgtService.filterCourses("CCC1");

        assertNotNull(response);
    }

    /**
     * Found via input fuzzing: CourseParticipantRequest.departments has no @NotEmpty/@NotNull (and
     * isn't even cascade-validated, since CourseRequest.courseParticipants lacks @Valid), so a
     * null departments list reached a bare for-each in getAllDepartments() and threw an NPE that
     * surfaced as a raw 500. A participant with no department scope is a legitimate case (parallel
     * to a classroom with no college meaning "university-wide") — it should create with zero org
     * units for that participant, not crash.
     */
    @Test
    @SneakyThrows
    void should_createCourse_whenParticipantDepartmentsIsNull() {
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(null).build());
        CourseRequest request = CourseRequest.builder()
                .courseTitle("Title With Null Departments")
                .courseUnit("0")
                .general(false)
                .shared(false)
                .batched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("NDEP1"))
                .expectedClassSize("500")
                .build();

        assertNotNull(mgtService.upsertCourse(request, Type.CREATE, null));

        SchedulableUnit course = courseService.getByTitle(request.getCourseTitle());
        assertTrue(course.getParticipants().getFirst().getOrgUnits().isEmpty());
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
                .expectedClassSize("500")
                .build();
    }
}