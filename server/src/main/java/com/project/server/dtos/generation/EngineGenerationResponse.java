package com.project.server.dtos.generation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Mirrors engine/app/models.py:GenerationResponse. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EngineGenerationResponse {
    private String status;
    private List<EngineAssignment> assignments;
    private List<String> infeasibleSessionIds;
}
