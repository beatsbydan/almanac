package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.response.DepartmentResponse;
import com.project.server.dtos.response.MessageResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private DepartmentMgtService mgtService;

    @Autowired
    private CollegeMgtService collegeMgtService;

    @Autowired
    private OrgUnitService departmentService;

    @Autowired
    private AuthMgtService authMgtService;

    private MessageResponse messageResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        collegeMgtService.createCollege(toCollegeRequest());
        messageResponse = mgtService.createDepartment(toDepartmentRequest());
    }

    @Test
    @SneakyThrows
    void should_create_department() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_update_department() {
        OrgUnit department = departmentService.getChildByName(toDepartmentRequest().getDepartmentName());
        messageResponse = mgtService.updateDepartment(department.getCode() , toDepartmentRequest());

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_get_departments() {
        List<DepartmentResponse> response = mgtService.getDepartments(toCollegeRequest().getCollegeName());

        assertNotNull(response);
    }

    /**
     * A staff/lecturer user (and, in the real app, the seeded ADMINISTRATOR) has no orgUnit at
     * all — getDepartments("") used to NPE on appUser.getOrgUnit().getParent() for exactly this
     * case, found by hitting the live endpoint as the admin user during load-test smoke testing.
     */
    @Test
    @SneakyThrows
    @WithMockUser(username = "staffWithNoDepartment")
    void should_get_departments_EMPTY_WHEN_CURRENT_USER_HAS_NO_ORG_UNIT() {
        authMgtService.createUser(StaffSignUpRequest.builder()
                .staffEmail("staffnodept@email.com")
                .password("password")
                .staffId("staffWithNoDepartment")
                .build());

        List<DepartmentResponse> response = mgtService.getDepartments("");

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @SneakyThrows
    void should_get_department() {
        List<DepartmentResponse> response = mgtService.getByDepartmentName("departmentName");

        assertNotNull(response);
    }

    private DepartmentRequest toDepartmentRequest(){
        return DepartmentRequest.builder()
                .departmentName("departmentName")
                .departmentShortName("departmentShortName")
                .college(toCollegeRequest().getCollegeName())
                .build();
    }

    private CollegeRequest toCollegeRequest(){
        return CollegeRequest.builder()
                .collegeName("collegeName")
                .collegeShortName("collegeShortName")
                .build();
    }
}