package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.College;
import com.project.server.db.models.Course;
import com.project.server.db.models.Department;
import com.project.server.db.services.college.CollegeService;
import com.project.server.db.services.course.CourseService;
import com.project.server.db.services.department.DepartmentService;
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
    private CourseService courseService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private CollegeService collegeService;
    private MessageResponse messageResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        College college = collegeService.save(College.builder().name("collegeName").shortName("collegeShortName").build());
        departmentService.save(Department.builder().name("department").shortName("DEPT").college(college).build());
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
        Course course = courseService.findByTitle(toCourseRequest().getCourseTitle());
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

    private CourseRequest toCourseRequest(){
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(List.of("department")).build());
        return CourseRequest.builder()
                .courseTitle("Title")
                .courseUnit("0")
                .isGeneral(false)
                .isShared(false)
                .isBatched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("CCC1","CCC2"))
                .expectedClassSize("500")
                .build();
    }
}