package com.project.server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    @NotBlank(message = "Staff ID cannot be blank")
    private String userId;
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
