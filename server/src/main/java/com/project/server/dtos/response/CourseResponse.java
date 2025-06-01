package com.project.server.dtos.response;

import com.project.server.dtos.request.course.CourseParticipantResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CourseResponse {
    private String code;

    private String courseTitle;

    private String courseUnit;

    private List<String> courseCodes;

    private List<CourseParticipantResponse> courseParticipants;

    private String expectedClassSize;

    private boolean shared;

    private boolean batched;

    private boolean general;
}
