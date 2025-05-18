package com.project.server.controllers;

import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.response.DepartmentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.DepartmentMgtService;
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
@RequestMapping("/api/v1/departments")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.DEPARTMENTS)
public class DepartmentController {
    private final DepartmentMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse createDepartment(@Valid @RequestBody DepartmentRequest request) throws DuplicateEntryException, NotFoundException {
        return mgtService.createDepartment(request);
    }

    @PutMapping("/update/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse updateDepartment(@PathVariable String code, @Valid @RequestBody DepartmentRequest request) throws NotFoundException {
        return mgtService.updateDepartment(code, request);
    }

    @GetMapping
    public List<DepartmentResponse> getDepartments(
            @RequestParam(name = "key") String college
    ) throws NotFoundException {
        return mgtService.getDepartments(college);
    }

    @GetMapping("/filter/{key}")
    public List<DepartmentResponse> getDepartment(
            @PathVariable String key
    ){
        return mgtService.getByDepartmentName(key);
    }
}
