package com.project.server.controllers;

import com.project.server.dtos.request.auth.*;
import com.project.server.dtos.response.AuthResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.dtos.response.StaffResponse;
import com.project.server.exceptions.*;
import com.project.server.services.AuthMgtService;
import com.project.server.util.AuthorityUtil;
import com.project.server.util.SwaggerApiTags;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = SwaggerApiTags.AUTH)
public class AuthController {
    private final AuthMgtService mgtService;

    @PostMapping("/register/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createUser(@RequestBody @Valid StaffSignUpRequest request) throws DuplicateEntryException, NotFoundException {
        return mgtService.createUser(request);
    }

    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createUser(@RequestBody @Valid StudentSignUpRequest request) throws DuplicateEntryException, NotFoundException {
        return mgtService.createUser(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request, HttpServletResponse httpResponse) throws ForbiddenException, NotFoundException {
        return mgtService.login(request, httpResponse);
    }

    @GetMapping("/staff")
    @PreAuthorize(AuthorityUtil.ADMINISTRATOR)
    public List<StaffResponse> getLecturers() {
        return mgtService.getLecturers();
    }

    @PutMapping("/student/update/{userId}")
    @PreAuthorize(AuthorityUtil.STUDENT)
    public MessageResponse updateCourseRepresentative(@PathVariable String userId, @RequestParam(value = "level") String level) throws NotFoundException {
        return mgtService.updateCourseRepresentative(userId, level);
    }

    @PutMapping("/student/deactivate/{userId}")
    @PreAuthorize(AuthorityUtil.STUDENT)
    public MessageResponse deactivateCourseRepresentative(@PathVariable String userId) throws NotFoundException {
        return mgtService.deactivateCourseRepresentative(userId);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestBody @Valid VerifyEmailRequest request, HttpServletResponse httpResponse) throws NotFoundException {
        return mgtService.verifyEmail(request, httpResponse);
    }

    @PostMapping("/verify-otp")
    public MessageResponse verifyOtp(@RequestBody @Valid VerifyOtpRequest request, HttpServletRequest httpRequest) throws Exception {
        return mgtService.verifyOtp(request, httpRequest);
    }

    @PutMapping("/reset-password")
    public MessageResponse resetPassword(@RequestBody @Valid ResetPasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        return mgtService.resetPassword(request, httpRequest, httpResponse);
    }

    @GetMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ForbiddenException {
        return mgtService.refresh(httpRequest, httpResponse);
    }
}
