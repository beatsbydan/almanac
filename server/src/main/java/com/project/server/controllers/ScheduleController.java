package com.project.server.controllers;

import com.project.server.dtos.request.ScheduleRequest;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.ScheduleResponse;
import com.project.server.dtos.response.SuggestionResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import com.project.server.services.schedule.ScheduleMgtService;
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
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Validated
@Tag(name  = SwaggerApiTags.SCHEDULES)
public class ScheduleController {
    private final ScheduleMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AuthorityUtil.STAFF)
    public MessageResponse createSchedule(@Valid @RequestBody ScheduleRequest scheduleRequest) throws NotFoundException, DuplicateEntryException, ScheduleUpsertException {
        return mgtService.createSchedule(scheduleRequest);
    }

    @PutMapping("/update/{code}")
    @PreAuthorize(AuthorityUtil.STAFF)
    public MessageResponse updateSchedule(
            @PathVariable String code,
            @Valid @RequestBody ScheduleRequest scheduleRequest
    ) throws NotFoundException, DuplicateEntryException, ScheduleUpsertException {
        return mgtService.updateSchedule(scheduleRequest, code);
    }

    @DeleteMapping("/{code}")
    @PreAuthorize((AuthorityUtil.STAFF))
    public MessageResponse deleteSchedule(
            @PathVariable String code
    ) throws NotFoundException{
        return mgtService.deleteSchedule(code);
    }

    @GetMapping
    @PreAuthorize((AuthorityUtil.STAFF))
    public List<ScheduleResponse> getSchedules(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "day",required = false) String day
    ){
        return mgtService.getSchedules(userId, day);
    }

    @GetMapping("/suggestions")
    @PreAuthorize((AuthorityUtil.STAFF))
    public List<SuggestionResponse> getSuggestions(
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "duration", required = false) String duration,
            @RequestParam(value = "college", required = false) String college,
            @RequestParam(value = "classSize", required = false) String classSize
    ){
        return mgtService.getSuggestions(day, startTime, duration, college, classSize);
    }

    @GetMapping("/{userId}")
    @PreAuthorize(AuthorityUtil.STUDENT)
    public List<ScheduleResponse> getSchedulesByCourseRepresentative(@PathVariable String userId) throws NotFoundException {
        return mgtService.getSchedulesByCourseRepresentative(userId);
    }

}
