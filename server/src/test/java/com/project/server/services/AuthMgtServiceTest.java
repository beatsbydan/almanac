package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.Department;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.department.DepartmentService;
import com.project.server.dtos.request.auth.LoginRequest;
import com.project.server.dtos.request.auth.ResetPasswordRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.dtos.response.AuthResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.util.VariableUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class AuthMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private DepartmentService departmentService;
    private MessageResponse messageResponse;
    private MessageResponse messageResponse2;
    private AuthResponse authResponse;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);

        departmentService.save(toDepartment());
        messageResponse = authMgtService.createUser(toStaffSignUpRequest());
        messageResponse2 = authMgtService.createUser(toStudentSignUpRequest());
        UserDetails userDetails = appUserService.loadUserByUsername(toStaffSignUpRequest().getStaffId());
        String token = authMgtService.generateAccessToken(userDetails);
        Cookie resetCookie = new Cookie(VariableUtil.RESET_TOKEN, token);
        Cookie refreshCookie = new Cookie(VariableUtil.REFRESH_TOKEN, token);

        when(httpRequest.getCookies()).thenReturn((new Cookie[]{resetCookie, refreshCookie}));
    }


    @Test
    @SneakyThrows
    void should_create_user_STAFF() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_create_user_STUDENT() {
        assertNotNull(messageResponse2);
    }

    @Test
    @SneakyThrows
    void should_update_student_info_SUCCESS(){
        messageResponse = authMgtService.updateCourseRepresentative(toStudentSignUpRequest().getStudentId(), "400");
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_deactivate_student_SUCCESS(){
        messageResponse = authMgtService.deactivateCourseRepresentative(toStudentSignUpRequest().getStudentId());
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void login() {
        authResponse = authMgtService.login(toLoginRequest(), httpResponse);
        assertNotNull(authResponse);
    }

    @Test
    @SneakyThrows
    void should_reset_password() {
        messageResponse = authMgtService.resetPassword(toResetPasswordRequest(), httpRequest, httpResponse);
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_refresh() {
        authResponse = authMgtService.refresh(httpRequest, httpResponse);
        assertNotNull(authResponse);
    }

    private StaffSignUpRequest toStaffSignUpRequest(){
        return StaffSignUpRequest.builder()
                .staffEmail("staff@email.com")
                .password("password")
                .staffId("staffId")
                .phone("phone")
                .name("name")
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

    private Department toDepartment(){
        return Department.builder()
                .name("department")
                .shortName("DEPT")
                .build();
    }
    private LoginRequest toLoginRequest(){
        return LoginRequest.builder()
                .password("password")
                .userId("staffId")
                .build();
    }
    private ResetPasswordRequest toResetPasswordRequest(){
        return ResetPasswordRequest.builder()
                .newPassword("password")
                .build();
    }
}