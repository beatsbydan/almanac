package com.project.server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "Password cannot be empty")
    private String newPassword;
}
