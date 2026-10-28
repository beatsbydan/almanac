package com.project.server.dtos.generation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/** Mirrors engine/app/models.py:Room — field-for-field, snake_case on the wire. */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EngineRoom {
    private String id;
    private int capacity;
}
