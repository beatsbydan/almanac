package com.project.server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpRequest {
    @NotBlank(message = "OTP cannot be empty")
    private String otp;
}
