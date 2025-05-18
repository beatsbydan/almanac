package com.project.server.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ScheduleRequest {
    @NotBlank(message = "Staff ID cannot be empty")
    private String userId;

    @NotBlank(message = "Day cannot be empty")
    private String recurringDay;

    @NotBlank(message = "Start time cannot be empty")
    private String startTime;

    @NotBlank(message = "Duration cannot be empty")
    private String timeDuration;

    @NotBlank(message = "Course code cannot be empty")
    private String courseCode;

    @NotEmpty(message = "Classroom(s) cannot be empty")
    private List<String> classroomCodes;
}
