package com.project.server.dtos.response.generation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnscheduledCourseResponse {
    private String code;
    private String title;
}
