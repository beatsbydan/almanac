package com.project.server.dtos.request.generation;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Triggers a bulk generation run. {@code lecturerAssignments} maps a course code to the user id
 * of whoever teaches it — the data model has no other source for "who teaches what" (that's
 * always been entered per-Schedule by a human, never stored against the course itself), so the
 * admin provides it here rather than the engine inventing an assignment it has no basis for.
 */
@Data
@Builder
public class GenerationTriggerRequest {
    @NotEmpty(message = "At least one lecturer assignment is required.")
    private Map<String, String> lecturerAssignments;
}
