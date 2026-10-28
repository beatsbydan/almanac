package com.project.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.BaseIntegrationTest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.CourseMgtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private CourseMgtService courseMgtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        super.setup();
    }

    @Test
    void should_create_course_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void should_update_course_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/courses/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void should_update_course_FAILURE_NOT_FOUND() throws Exception {
        doThrow(new NotFoundException("Level / Course not found"))
                .when(courseMgtService).upsertCourse(any(CourseRequest.class), any(Type.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/courses/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void should_update_course_FAILURE_DUPLICATE() throws Exception {
        doThrow(new DuplicateEntryException("Course already exists"))
                .when(courseMgtService).upsertCourse(any(CourseRequest.class), any(Type.class), any(String.class));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/courses/update/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    /**
     * Found via input fuzzing: expectedClassSize/courseUnit are Strings with only @NotBlank (no
     * numeric format check), so "not-a-number" reached a bare Integer.parseInt/valueOf deep in
     * CourseMgtService and threw NumberFormatException, which fell through to the generic 500
     * handler. Advice now translates it to 400.
     */
    @Test
    void should_create_course_FAILURE_NUMBER_FORMAT() throws Exception {
        doThrow(new NumberFormatException("For input string: \"not-a-number\""))
                .when(courseMgtService).upsertCourse(any(CourseRequest.class), any(Type.class), any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * Found via input fuzzing: a 5000-character course title has no @Size limit and reaches the
     * database uncaught, where the title column's varchar(255) bound rejects it as a
     * DataIntegrityViolationException — previously a bare 500 leaking a Postgres error message.
     */
    @Test
    void should_create_course_FAILURE_DATA_INTEGRITY() throws Exception {
        doThrow(new DataIntegrityViolationException("value too long for type character varying(255)"))
                .when(courseMgtService).upsertCourse(any(CourseRequest.class), any(Type.class), any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCourseRequest())))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_get_courses_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/courses")
                        .param("unit", "0")
                        .param("department", "department")
                        .param("level", "level")
                        .param("isShared", "false")
                        .param("isBatched", "false")
                        .param("isGeneral", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void should_get_courses_FAILURE_TYPE_MISMATCH() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/courses")
                        .param("isShared", "not-a-boolean")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void should_filter_courses_SUCCESS() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/courses/filter/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private CourseRequest toCourseRequest(){
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(List.of("department")).build());
        return CourseRequest.builder()
                .courseTitle("Title")
                .courseUnit("0")
                .general(false)
                .shared(false)
                .batched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of("CCC1","CCC2"))
                .expectedClassSize("500")
                .build();
    }
}