package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.ClassroomMgtService;
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

class ClassroomControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private ClassroomMgtService mgtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_classroom_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/classrooms/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toClassRoomRequest())))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_create_classroom_FAILURE_DUPLICATE_ERROR() throws Exception {
        doThrow(new DuplicateEntryException("Classroom already exists"))
                .when(mgtService).createClassroom(any(ClassroomRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/classrooms/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toClassRoomRequest())))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_classroom_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/classrooms/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toClassRoomRequest())))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_update_classroom_FAILURE_NOT_FOUND_ERROR() throws Exception {
        doThrow(new NotFoundException("Classroom not found"))
                .when(mgtService).updateClassroom(any(String.class), any(ClassroomRequest.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/classrooms/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toClassRoomRequest())))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void should_filter_classrooms_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/classrooms")
                .param("college", "college")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_filter_classrooms_by_college_or_name_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/classrooms/filter/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private ClassroomRequest toClassRoomRequest(){
        return ClassroomRequest.builder()
                .className("classname")
                .classSize(200)
                .college("college")
                .build();
    }
}