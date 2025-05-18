package com.project.server.dtos.memory;

import com.project.server.db.models.ClassRoom;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Suggestion {
    private String day;

    private String time;

    private String startTime;

    private String duration;

    private ClassRoom classRoom;
}
