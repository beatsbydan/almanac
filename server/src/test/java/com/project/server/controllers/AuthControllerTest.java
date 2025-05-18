package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.auth.LoginRequest;
import com.project.server.dtos.request.auth.ResetPasswordRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.ForbiddenException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.AuthMgtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private AuthMgtService authMgtService;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    void should_create_staff_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStaffSignUpRequest())))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void should_create_student_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStudentSignUpRequest())))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void should_create_staff_DUPLICATE_ERROR() throws Exception {
        doThrow(new DuplicateEntryException("User already exists"))
                .when(authMgtService).createUser(any(StaffSignUpRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStaffSignUpRequest())))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    void should_create_staff_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("Department not found"))
                .when(authMgtService).createUser(any(StaffSignUpRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStaffSignUpRequest())))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void should_create_student_DUPLICATE_ERROR() throws Exception {
        doThrow(new DuplicateEntryException("User already exists"))
                .when(authMgtService).createUser(any(StudentSignUpRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStaffSignUpRequest())))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    void should_create_student_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("Department not found"))
                .when(authMgtService).createUser(any(StudentSignUpRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toStaffSignUpRequest())))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void should_login_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toLoginRequest())))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void should_login_FORBIDDEN_ERROR() throws Exception {
        doThrow(new ForbiddenException("Invalid credentials"))
                .when(authMgtService).login(any(LoginRequest.class), any(HttpServletResponse.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toLoginRequest())))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void should_login_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("User was not found"))
                .when(authMgtService).login(any(LoginRequest.class), any(HttpServletResponse.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toLoginRequest())))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void should_update_student_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/student/update/123")
                        .param("level", "400")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void should_update_student_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("User was not found"))
                .when(authMgtService).updateCourseRepresentative(any(String.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/student/update/123")
                        .param("level","400")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void should_deactivate_student_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/student/deactivate/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void should_deactivate_student_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("User was not found"))
                .when(authMgtService).deactivateCourseRepresentative(any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/student/deactivate/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void should_reset_password_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toResetPasswordRequest())))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void should_reset_password_ERROR() throws Exception {
        doThrow(new Exception("Something went wrong"))
                .when(authMgtService).resetPassword(any(ResetPasswordRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toResetPasswordRequest())))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    private StaffSignUpRequest toStaffSignUpRequest(){
        return StaffSignUpRequest.builder()
                .staffEmail("email@email.com")
                .password("password")
                .staffId("staffId")
                .phone("phone")
                .name("name")
                .build();
    }

    private StudentSignUpRequest toStudentSignUpRequest(){
        return StudentSignUpRequest.builder()
                .studentEmail("email@email.com")
                .password("password")
                .studentId("staffId")
                .department("department")
                .level("100")
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