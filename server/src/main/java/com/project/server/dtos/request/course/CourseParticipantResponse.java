package com.project.server.dtos.request.course;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CourseParticipantResponse {
    private String level;

    private List<String> departments;
}
