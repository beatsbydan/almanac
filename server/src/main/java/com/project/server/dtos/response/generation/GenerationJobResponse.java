package com.project.server.dtos.response.generation;

import com.project.server.enums.GenerationJobStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenerationJobResponse {
    private String code;
    private GenerationJobStatus status;
    private Integer scheduledCount;
    private String message;
    private List<String> infeasibleUnitCodes;
    private List<String> skippedNoTeacherUnitCodes;
}
