package com.project.server.db.services.appUser;

import com.project.server.db.models.AppUser;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.exceptions.NotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AppUserService extends UserDetailsService {
    void save(AppUser user);
    boolean isExistingUser(StaffSignUpRequest request);
    boolean isExistingUser(StudentSignUpRequest request);
    AppUser getByUserIdOrEmail(String userId, String staffEmail) throws NotFoundException;
    Optional<AppUser> getByUserIdOpt(String userId);
    boolean hasExistingCourseRepresentative(StudentSignUpRequest request);
    boolean isExistingAdmin();
}
