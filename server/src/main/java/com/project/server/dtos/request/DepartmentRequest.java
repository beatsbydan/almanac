package com.project.server.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DepartmentRequest {
    @NotBlank(message = "Department name cannot be empty")
    private String departmentName;

    @NotBlank(message = "Department short name cannot be empty")
    private String departmentShortName;

    private String college;
}
