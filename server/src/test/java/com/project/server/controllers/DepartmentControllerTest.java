package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.DepartmentMgtService;
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

class DepartmentControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private DepartmentMgtService mgtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_department_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/departments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDepartmentRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_department_FAILURE_DUPLICATE_ERROR() throws Exception {
        doThrow(new DuplicateEntryException("Department already exists"))
                .when(mgtService).createDepartment(any(DepartmentRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/departments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDepartmentRequest())))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_department_FAILURE_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("College not found"))
                .when(mgtService).createDepartment(any(DepartmentRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/departments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDepartmentRequest())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_department_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/departments/update/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDepartmentRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_department_FAILURE_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("College not found"))
                .when(mgtService).updateDepartment(any(String.class), any(DepartmentRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/departments/update/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDepartmentRequest())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_departments_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments")
                        .param("key", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void should_get_departments_FAILURE_MISSING_REQUIRED_PARAM() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_get_department_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments/filter/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private DepartmentRequest toDepartmentRequest(){
        return DepartmentRequest.builder()
                .departmentName("departmentName")
                .departmentShortName("departmentShortName")
                .build();
    }
}