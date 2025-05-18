package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.Department;
import com.project.server.db.services.department.DepartmentService;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.response.DepartmentResponse;
import com.project.server.dtos.response.MessageResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private DepartmentMgtService mgtService;

    @Autowired
    private CollegeMgtService collegeMgtService;

    @Autowired
    private DepartmentService departmentService;

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
        Department department = departmentService.getByName(toDepartmentRequest().getDepartmentName());
        messageResponse = mgtService.updateDepartment(department.getCode() , toDepartmentRequest());

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_get_departments() {
        List<DepartmentResponse> response = mgtService.getDepartments(toCollegeRequest().getCollegeName());

        assertNotNull(response);
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