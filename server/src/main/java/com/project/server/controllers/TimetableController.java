package com.project.server.controllers;

import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.response.DocumentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.TimeTableResponse;
import com.project.server.exceptions.*;
import com.project.server.services.timetable.TimeTableMgtService;
import com.project.server.util.AuthorityUtil;
import com.project.server.util.SwaggerApiTags;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/timetables")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.TIMETABLES)
@Slf4j
public class TimetableController {
    private final TimeTableMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse createTimeTable(@Valid @RequestBody TimetableRequest request) throws DuplicateEntryException, BadRequestException {
        return mgtService.createTimeTable(request);
    }

    @PutMapping("/update/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse updateTimeTable(@PathVariable String code, @Valid @RequestBody TimetableRequest request) throws NotFoundException{
        return mgtService.updateTimeTable(code, request);
    }

    @PutMapping("/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse changeStatus(@PathVariable String code, @RequestParam(name = "status") String status) throws NotFoundException {
        return mgtService.changeScheduleCreationStatus(code, status);
    }

    @PutMapping("/deactivate")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse deactivate(){
        return mgtService.deactivate();
    }

    @GetMapping("/active")
    public TimeTableResponse getActiveTimeTable(){
        return mgtService.getActiveTimeTable();
    }

    @GetMapping("/download/{code}")
    public ResponseEntity<byte[]> download(
            @PathVariable String code,
            @RequestParam(name = "userId") String userId
    ) throws NotFoundException, TimeTableGenerationException {
        DocumentResponse documentResponse = mgtService.generateTimeTable(code, userId);

        String fileName = documentResponse.getFileName();
        if (!fileName.endsWith(".docx")) {
            fileName += ".docx";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(documentResponse.getBytesArr());
    }
}
