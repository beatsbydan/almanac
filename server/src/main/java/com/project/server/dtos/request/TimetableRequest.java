package com.project.server.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimetableRequest {
    @NotBlank(message = "Session cannot be empty")
    private String session;

    @NotBlank(message = "Semester cannot be empty")
    private String semester;

    @NotBlank(message = "Creation start date cannot be empty")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String creationStartDate;

    @NotBlank(message = "Creation start date cannot be empty")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String creationEndDate;
}
