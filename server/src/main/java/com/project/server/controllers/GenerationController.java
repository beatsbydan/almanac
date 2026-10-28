package com.project.server.controllers;

import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationJobResponse;
import com.project.server.dtos.response.generation.UnscheduledCourseResponse;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.generation.GenerationOrchestrationService;
import com.project.server.util.AuthorityUtil;
import com.project.server.util.SwaggerApiTags;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/generation")
@RequiredArgsConstructor
@Validated
@Tag(name = SwaggerApiTags.GENERATION)
public class GenerationController {
    private final GenerationOrchestrationService generationOrchestrationService;

    @GetMapping("/unscheduled")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public List<UnscheduledCourseResponse> getUnscheduledCourses() {
        return generationOrchestrationService.getUnscheduledCourses();
    }

    /**
     * Returns as soon as the job is recorded (PENDING) rather than waiting for the solve to
     * finish — poll getJob() for the result. See GenerationOrchestrationService.trigger().
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public GenerationJobResponse generate(@Valid @RequestBody GenerationTriggerRequest request) {
        return generationOrchestrationService.trigger(request);
    }

    @GetMapping("/jobs/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public GenerationJobResponse getJob(@PathVariable String code) throws NotFoundException {
        return generationOrchestrationService.getJob(code);
    }
}
