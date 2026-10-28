package com.project.server.dtos.generation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mirrors engine/app/models.py:Assignment — deserialized from the engine's response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EngineAssignment {
    private String sessionId;
    private String day;
    private int startHour;
    private String roomId;
}
