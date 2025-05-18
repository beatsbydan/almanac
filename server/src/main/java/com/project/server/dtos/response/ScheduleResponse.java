package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ScheduleResponse {
    private String code;

    private CourseResponse course;

    private List<ClassroomResponse> classrooms;

    private String day;

    private String time;
}
