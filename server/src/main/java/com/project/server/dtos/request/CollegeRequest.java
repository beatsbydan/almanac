package com.project.server.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CollegeRequest {
    @NotBlank(message = "College name cannot be empty")
    private String collegeName;

    @NotBlank(message = "College short name cannot be empty")
    private String collegeShortName;
}
