package com.project.server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StudentSignUpRequest {
    @NotBlank(message = "Student Email cannot be empty")
    private String studentEmail;

    @NotBlank(message = "Student ID cannot be empty")
    private String studentId;

    @NotBlank(message = "Level cannot be empty")
    private String level;

    @NotBlank(message = "Department cannot be empty")
    private String department;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}