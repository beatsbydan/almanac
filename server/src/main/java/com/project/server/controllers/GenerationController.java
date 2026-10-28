package com.project.server.controllers;

import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationResultResponse;
import com.project.server.dtos.response.generation.UnscheduledCourseResponse;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import com.project.server.services.generation.GenerationOrchestrationService;
import com.project.server.util.AuthorityUtil;
import com.project.server.util.SwaggerApiTags;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public GenerationResultResponse generate(@Valid @RequestBody GenerationTriggerRequest request) throws NotFoundException, ScheduleUpsertException {
        return generationOrchestrationService.generate(request);
    }
}
