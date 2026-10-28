package com.project.server.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomRequest {
    @NotBlank(message = "Class name cannot be empty")
    private String className;

    @Positive(message = "Class size must be greater than zero")
    private int classSize;

    private String college;
}
