package com.project.server.dtos.generation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Mirrors engine/app/models.py:Session. */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EngineSession {
    private String id;
    private String teacherId;
    private int durationHours;
    private int expectedSize;
    private List<String> eligibleRoomIds;
    private List<String> participantGroupIds;
}
