package com.project.server.dtos.request.course;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class CourseParticipantRequest {
    private String level;

    private List<String> departments;
}
