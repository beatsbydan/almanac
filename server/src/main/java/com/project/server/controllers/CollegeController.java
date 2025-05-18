package com.project.server.controllers;

import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.response.CollegeResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.CollegeMgtService;
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
@RequestMapping("/api/v1/colleges")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.COLLEGES)
public class CollegeController {
    private final CollegeMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse createCollege(@Valid @RequestBody CollegeRequest request) throws DuplicateEntryException {
        return mgtService.createCollege(request);
    }

    @PutMapping("/update/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse updateCollege(@PathVariable String code, @Valid @RequestBody CollegeRequest request) throws NotFoundException {
        return mgtService.updateCollege(code, request);
    }

    @GetMapping
    public List<CollegeResponse> getColleges(){
        return mgtService.getColleges();
    }
}
