package com.project.server.db.services.appUser;

import com.project.server.db.models.AppUser;
import com.project.server.db.repositories.AppUserRepository;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.auth.StudentSignUpRequest;
import com.project.server.enums.Role;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserServiceImpl implements AppUserService{
    private final AppUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Staff with ID: %s was not found ", userId)));
    }

    @Override
    public void save(AppUser user) {
        repository.save(user);
    }

    @Override
    public boolean isExistingUser(StaffSignUpRequest request) {
        return repository.findByEmailOrId(request.getStaffEmail(), request.getStaffId()).isPresent();
    }

    @Override
    public boolean isExistingUser(StudentSignUpRequest request) {
        return repository.findByEmailOrId(request.getStudentEmail(), request.getStudentId()).isPresent();
    }

    @Override
    public AppUser getByUserIdOrEmail(String userId, String userEmail) throws NotFoundException {
        return repository.findByEmailOrId(userEmail, userId)
                .orElseThrow(() -> new NotFoundException("User not found.."));
    }

    @Override
    public Optional<AppUser> getByUserIdOpt(String userId) {
        return repository.findByEmailOrId(userId, null);
    }

    @Override
    public boolean hasExistingCourseRepresentative(StudentSignUpRequest request) {
        return repository.hasExistingCourseRepresentative(request.getDepartment(), request.getLevel()).isPresent();
    }

    @Override
    public boolean isExistingAdmin() {
        return repository.isExistingAdmin(Role.ADMINISTRATOR).isPresent();
    }
}
