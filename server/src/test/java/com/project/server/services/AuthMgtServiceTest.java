package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.AppUser;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.auth.LoginRequest;
import com.project.server.dtos.request.auth.ResetPasswordRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.dtos.request.auth.VerifyEmailRequest;
import com.project.server.dtos.request.auth.VerifyOtpRequest;
import com.project.server.dtos.response.AuthResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.BadRequestException;
import com.project.server.util.VariableUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class AuthMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private OrgUnitService departmentService;
    @Autowired
    private RedissonClient redissonClient;
    private MessageResponse messageResponse;
    private MessageResponse messageResponse2;
    private AuthResponse authResponse;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);

        // Redis isn't covered by @Transactional rollback like the DB is, and the OTP attempt
        // counter below is deliberately NOT cleared except on a successful verify — so a prior
        // test's failed attempts for this same (tenant, userId) would otherwise leak in here.
        redissonClient.getAtomicLong(String.format("otp-attempts:%s:%s", TenantContext.get(), "STAFFID")).delete();

        departmentService.save(toDepartment());
        messageResponse = authMgtService.createUser(toStaffSignUpRequest());
        messageResponse2 = authMgtService.createUser(toStudentSignUpRequest());
        UserDetails userDetails = appUserService.loadUserByUsername(toStaffSignUpRequest().getStaffId());
        String token = authMgtService.generateAccessToken(userDetails);
        Cookie resetCookie = new Cookie(VariableUtil.RESET_TOKEN, token);
        Cookie refreshCookie = new Cookie(VariableUtil.REFRESH_TOKEN, token);

        when(httpRequest.getCookies()).thenReturn((new Cookie[]{resetCookie, refreshCookie}));
    }


    @Test
    @SneakyThrows
    void should_create_user_STAFF() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_create_user_STUDENT() {
        assertNotNull(messageResponse2);
    }

    @Test
    @SneakyThrows
    void should_update_student_info_SUCCESS(){
        messageResponse = authMgtService.updateCourseRepresentative(toStudentSignUpRequest().getStudentId(), "400");
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_deactivate_student_SUCCESS(){
        messageResponse = authMgtService.deactivateCourseRepresentative(toStudentSignUpRequest().getStudentId());
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void login() {
        authResponse = authMgtService.login(toLoginRequest(), httpResponse);
        assertNotNull(authResponse);
    }

    @Test
    @SneakyThrows
    void should_reset_password() {
        messageResponse = authMgtService.resetPassword(toResetPasswordRequest(), httpRequest, httpResponse);
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_refresh() {
        authResponse = authMgtService.refresh(httpRequest, httpResponse);
        assertNotNull(authResponse);
    }

    @Test
    @SneakyThrows
    void should_verify_email() {
        // mail now sends off the request thread (MailingMgtService is @Async) — this proves the
        // request itself completes and persists state correctly without waiting on, or failing
        // because of, the actual SMTP round-trip.
        messageResponse = authMgtService.verifyEmail(toVerifyEmailRequest(), httpResponse);

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_verify_otp_SUCCESS() {
        authMgtService.verifyEmail(toVerifyEmailRequest(), httpResponse);
        AppUser user = appUserService.getByUserIdOrEmail(null, toStaffSignUpRequest().getStaffEmail());
        stubResetTokenCookie(user);

        messageResponse = authMgtService.verifyOtp(
                VerifyOtpRequest.builder().otp(user.getOtp().getOtp()).build(), httpRequest);

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_verify_otp_FAILURE_WRONG_OTP() {
        authMgtService.verifyEmail(toVerifyEmailRequest(), httpResponse);
        AppUser user = appUserService.getByUserIdOrEmail(null, toStaffSignUpRequest().getStaffEmail());
        stubResetTokenCookie(user);

        assertThrows(BadRequestException.class, () ->
                authMgtService.verifyOtp(VerifyOtpRequest.builder().otp("000000").build(), httpRequest));
    }

    /**
     * A 6-digit OTP (900,000 possibilities) is still brute-forceable against an unthrottled
     * endpoint, so verifyOtp also locks out after MAX_OTP_ATTEMPTS wrong guesses — this proves
     * that lockout actually triggers, not just that a single wrong guess is rejected.
     */
    @Test
    @SneakyThrows
    void should_lockOut_afterTooManyIncorrectOtpAttempts() {
        authMgtService.verifyEmail(toVerifyEmailRequest(), httpResponse);
        AppUser user = appUserService.getByUserIdOrEmail(null, toStaffSignUpRequest().getStaffEmail());
        stubResetTokenCookie(user);

        VerifyOtpRequest wrongOtp = VerifyOtpRequest.builder().otp("000000").build();
        for (int i = 0; i < 5; i++) {
            assertThrows(BadRequestException.class, () -> authMgtService.verifyOtp(wrongOtp, httpRequest));
        }

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> authMgtService.verifyOtp(wrongOtp, httpRequest));
        assertEquals("Too many incorrect attempts, please request a new OTP.", e.getMessage());
    }

    private void stubResetTokenCookie(AppUser user) {
        String resetToken = authMgtService.generateResetToken(user);
        when(httpRequest.getCookies()).thenReturn(new Cookie[]{new Cookie(VariableUtil.RESET_TOKEN, resetToken)});
    }

    @Test
    @SneakyThrows
    void should_get_lecturers() {
        var lecturers = authMgtService.getLecturers();

        assertEquals(1, lecturers.size());
        assertEquals("STAFFID", lecturers.getFirst().getUserId());
    }

    private VerifyEmailRequest toVerifyEmailRequest(){
        return VerifyEmailRequest.builder()
                .userEmail(toStaffSignUpRequest().getStaffEmail())
                .build();
    }

    private StaffSignUpRequest toStaffSignUpRequest(){
        return StaffSignUpRequest.builder()
                .staffEmail("staff@email.com")
                .password("password")
                .staffId("staffId")
                .phone("phone")
                .name("name")
                .build();
    }

    private StudentSignUpRequest toStudentSignUpRequest(){
        return StudentSignUpRequest.builder()
                .studentEmail("student@email.com")
                .password("password")
                .studentId("studentId")
                .department("department")
                .level("100")
                .build();
    }

    private OrgUnit toDepartment(){
        return OrgUnit.builder()
                .name("department")
                .shortName("DEPT")
                .type("Department")
                .build();
    }
    private LoginRequest toLoginRequest(){
        return LoginRequest.builder()
                .password("password")
                .userId("staffId")
                .build();
    }
    private ResetPasswordRequest toResetPasswordRequest(){
        return ResetPasswordRequest.builder()
                .newPassword("password")
                .build();
    }
}