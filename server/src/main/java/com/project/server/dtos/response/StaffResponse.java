package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResponse {
    private String userId;
    private String name;
    private String email;
}
