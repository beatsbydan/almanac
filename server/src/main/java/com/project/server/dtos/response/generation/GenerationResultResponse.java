package com.project.server.dtos.response.generation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenerationResultResponse {
    private int scheduledCount;
    private List<String> infeasibleUnitCodes;
    private List<String> skippedNoTeacherUnitCodes;
    private String message;
}
