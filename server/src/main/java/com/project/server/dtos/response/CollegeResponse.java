package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CollegeResponse {
    private String code;

    private String collegeName;

    private String collegeShortName;
}
