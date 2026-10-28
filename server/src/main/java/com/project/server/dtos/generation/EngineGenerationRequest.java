package com.project.server.dtos.generation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Mirrors engine/app/models.py:GenerationRequest — the payload POSTed to the engine's /generate. */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EngineGenerationRequest {
    private String tenantId;
    private List<String> days;
    private List<Integer> periodStartHours;
    private int maxEndHour;
    private List<EngineBlackoutWindow> blackoutWindows;
    private List<EngineRoom> rooms;
    private List<EngineSession> sessions;
}
