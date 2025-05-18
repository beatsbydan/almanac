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

    private boolean isShared;

    private boolean isBatched;

    private boolean isGeneral;
}
