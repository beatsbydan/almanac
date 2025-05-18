package com.project.server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffSignUpRequest {
    @NotBlank(message = "Staff Email cannot be empty")
    private String staffEmail;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Phone cannot be empty")
    private String phone;

    @NotBlank(message = "Staff ID cannot be empty")
    private String staffId;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
