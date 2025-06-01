package com.project.server.dtos.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseRequest {
    @NotBlank(message = "Course Title cannot be empty.")
    private String courseTitle;

    @NotBlank(message = "Course Unit cannot be empty.")
    private String courseUnit;

    @NotEmpty(message = "Course Codes cannot be empty.")
    private List<String> courseCodes;

    @NotEmpty(message = "Course Participants cannot be empty")
    private List<CourseParticipantRequest> courseParticipants;

    @NotBlank(message = "Class size cannot be empty.")
    private String expectedClassSize;

    @NotNull(message = "isShared cannot be null.")
    private boolean shared;

    @NotNull(message = "isBatched cannot be null.")
    private boolean batched;

    @NotNull(message = "isGeneral cannot be null.")
    private boolean general;
}