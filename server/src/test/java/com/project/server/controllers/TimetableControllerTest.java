package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.exceptions.*;
import com.project.server.services.timetable.TimeTableMgtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimetableControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private TimeTableMgtService timeTableMgtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_timetable_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/timetables/create")
                .content(objectMapper.writeValueAsString(toTimeTableRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_timetable_FAILURE_DUPLICATE() throws Exception {
        doThrow(new DuplicateEntryException("Timetable already exists"))
                .when(timeTableMgtService).createTimeTable(any(TimetableRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/timetables/create")
                .content(objectMapper.writeValueAsString(toTimeTableRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_timetable_FAILURE_BAD_REQUEST() throws Exception {
        doThrow(new BadRequestException("Bad Request"))
                .when(timeTableMgtService).createTimeTable(any(TimetableRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/timetables/create")
                .content(objectMapper.writeValueAsString(toTimeTableRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_timetable_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/update/123")
                .content(objectMapper.writeValueAsString(toTimeTableRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_timetable_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("Timetable not found"))
                .when(timeTableMgtService).updateTimeTable(any(String.class), any(TimetableRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/update/123")
                .content(objectMapper.writeValueAsString(toTimeTableRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_initiate_generation_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/initiate-generation")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_initiate_generation_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new ScheduleUpsertException("Timetable not found"))
                .when(timeTableMgtService).initiateGeneration();
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/initiate-generation")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_change_schedule_creation_status_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/123")
                        .param("status", "ENABLED")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_change_schedule_creation_status_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("Timetable not found"))
                .when(timeTableMgtService).changeScheduleCreationStatus(any(String.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/123")
                        .param("status", "ENABLED")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_active_timetable_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/timetables/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void should_download_timetable_SUCCESS() throws Exception {
        when(timeTableMgtService.generateAndDownloadUserTimeTable(any(String.class), any(String.class)))
                .thenReturn(new DocumentResponse(objectMapper.writeValueAsBytes(""), "filename"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/timetables/download/user/123")
                        .param("userId", "userId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void should_download_timetable_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("Timetable does not exist"))
                .when(timeTableMgtService).generateAndDownloadUserTimeTable(any(String.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/timetables/download/user/123")
                        .param("userId", "userId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void should_download_timetable_FAILURE_GENERATION() throws Exception {
        doThrow(new TimeTableGenerationException("Bad Request"))
                .when(timeTableMgtService).generateAndDownloadUserTimeTable(any(String.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/timetables/download/user/123")
                        .param("userId", "userId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_deactivate_timetable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/timetables/deactivate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private TimetableRequest toTimeTableRequest(){
        return TimetableRequest.builder()
                .session("2024/2025")
                .semester("FIRST")
                .creationStartDate("2024-06-09")
                .creationEndDate("2024-06-12")
                .build();
    }
}