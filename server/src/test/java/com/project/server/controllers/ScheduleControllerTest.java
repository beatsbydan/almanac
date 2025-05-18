package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.ScheduleRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import com.project.server.services.schedule.ScheduleMgtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScheduleControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private ScheduleMgtService mgtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_create_schedule_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isCreated()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_create_schedule_FAILURE_DUPLICATE() throws Exception {
        doThrow(new DuplicateEntryException("Schedule exists"))
                .when(mgtService).createSchedule(any(ScheduleRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isConflict()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_create_schedule_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("A parameter was not found."))
                .when(mgtService).createSchedule(any(ScheduleRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isNotFound()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_create_schedule_FAILURE_UPSERT() throws Exception {
        doThrow(new ScheduleUpsertException("Schedule exists"))
                .when(mgtService).createSchedule(any(ScheduleRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/schedules/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isBadRequest()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_update_schedule_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/schedules/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isOk()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_update_schedule_FAILURE_DUPLICATE() throws Exception {
        doThrow(new DuplicateEntryException("Schedule exists"))
                .when(mgtService).updateSchedule(any(ScheduleRequest.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/schedules/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isConflict()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_update_schedule_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("A parameter was not found."))
                .when(mgtService).updateSchedule(any(ScheduleRequest.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/schedules/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isNotFound()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_update_schedule_FAILURE_UPSERT() throws Exception {
        doThrow(new ScheduleUpsertException("Schedule exists"))
                .when(mgtService).updateSchedule(any(ScheduleRequest.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/schedules/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toScheduleRequest())))
                .andDo(print())
                .andExpect((status().isBadRequest()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_get_schedules_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedules")
                        .param("userId", "userId")
                        .param("day", "Monday")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect((status().isOk()));
    }

    @Test
    @WithMockUser(roles = {"LECTURER"})
    void should_get_schedule_suggestions_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedules/suggestions")
                        .param("startTime", "8:00am")
                        .param("duration", "2 hours")
                        .param("college", "college")
                        .param("classSize", "30")
                        .param("day", "Monday")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect((status().isOk()));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void should_get_schedules_by_course_representative_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/schedules/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect((status().isOk()));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_delete_schedule_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/schedules/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect((status().isOk()));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_delete_schedule_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("Schedule not found"))
                .when(mgtService).deleteSchedule(any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/schedules/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect((status().isNotFound()));
    }

    private ScheduleRequest toScheduleRequest(){
        return ScheduleRequest.builder()
                .userId("staffId")
                .courseCode("SEN101")
                .classroomCodes(List.of("code"))
                .recurringDay("Monday")
                .startTime("8:00am")
                .timeDuration("2")
                .build();
    }
}