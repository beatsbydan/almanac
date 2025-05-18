package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DepartmentResponse {
    private String code;

    private String college;

    private String departmentName;

    private String departmentShortName;
}
