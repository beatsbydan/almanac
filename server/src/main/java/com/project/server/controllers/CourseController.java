package com.project.server.controllers;

import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.CourseResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.services.CourseMgtService;
import com.project.server.util.SwaggerApiTags;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.COURSES)
public class CourseController {
    private final CourseMgtService mgtService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createCourse(@Valid @RequestBody CourseRequest request) throws DuplicateEntryException, NotFoundException {
        return mgtService.upsertCourse(request, Type.CREATE, null);
    }

    @PutMapping("/update/{code}")
    public MessageResponse updateCourse(@PathVariable String code, @Valid @RequestBody CourseRequest request) throws NotFoundException, DuplicateEntryException {
        return mgtService.upsertCourse(request, Type.UPDATE, code);
    }

    @GetMapping("/filter/{key}")
    public List<CourseResponse> filterCourses(
            @PathVariable("key") String key
    ){
        return mgtService.filterCourses(key);
    }

    @GetMapping
    public List<CourseResponse> getCourses(
            @RequestParam(value = "courseUnit", required = false) String unit,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "isShared", required = false) boolean shared,
            @RequestParam(value = "isBatched", required = false) boolean batched,
            @RequestParam(value = "isGeneral", required = false) boolean general
    ){
        return mgtService.getCourses(unit, department, level, shared, batched, general);
    }
}