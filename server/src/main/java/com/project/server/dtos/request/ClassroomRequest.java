package com.project.server.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomRequest {
    @NotBlank(message = "Class name cannot be empty")
    private String className;

    @NotBlank(message = "Class size cannot be empty")
    private int classSize;

    private String college;
}
