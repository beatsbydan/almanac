package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String userId;

    private String userEmail;

    private String role;

    private String department;

    private String level;

    private String token;
}
