package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuggestionResponse {
    private ClassroomResponse classRoom;

    private String day;

    private String time;

    private String duration;

    private String startTime;
}
