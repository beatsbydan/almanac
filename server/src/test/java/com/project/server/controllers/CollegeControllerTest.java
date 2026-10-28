package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.CollegeMgtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CollegeControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private CollegeMgtService mgtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_college_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/colleges/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCollegeRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_college_FAILURE_DUPLICATE_ERROR() throws Exception {
        doThrow(new DuplicateEntryException("College already exists"))
                .when(mgtService).createCollege(any(CollegeRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/colleges/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCollegeRequest())))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_college_FAILURE_VALIDATION_ERROR() throws Exception {
        CollegeRequest blank = CollegeRequest.builder().collegeName("").collegeShortName("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/colleges/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blank)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.errors.collegeName").value("College name cannot be empty"))
                .andExpect(jsonPath("$.result.errors.collegeShortName").value("College short name cannot be empty"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_college_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/colleges/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCollegeRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_college_FAILURE_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("College not found"))
                .when(mgtService).updateCollege(any(String.class), any(CollegeRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/colleges/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCollegeRequest())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_colleges_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/colleges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private CollegeRequest toCollegeRequest(){
        return CollegeRequest.builder()
                .collegeName("collegeName")
                .collegeShortName("collegeShortName")
                .build();
    }
}