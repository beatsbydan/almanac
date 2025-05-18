package com.project.server.controllers;

import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.response.ClassroomResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.ClassroomMgtService;
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
@RequestMapping("/api/v1/classrooms")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.CLASSROOMS)
public class ClassroomController {
    private final ClassroomMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse createClassRoom (@RequestBody @Valid ClassroomRequest request) throws DuplicateEntryException, NotFoundException {
        return mgtService.createClassroom(request);
    }

    @PutMapping("/update/{code}")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public MessageResponse updateClassRoom(@PathVariable String code, @RequestBody @Valid ClassroomRequest request) throws NotFoundException{
        return mgtService.updateClassroom(code, request);
    }

    @GetMapping("/filter/{key}")
    @PreAuthorize(AuthorityUtil.STAFF)
    public List<ClassroomResponse> filterClassroomsByNameOrCollege(
            @PathVariable("key") String key
    ){
        return mgtService.filterClassrooms(key);
    }

    @GetMapping
    public List<ClassroomResponse> filterClassrooms(
            @RequestParam(name = "college", required = false) String college
    ){
        return mgtService.findClassroomsByCollege(college);
    }
}
