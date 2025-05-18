package com.project.server.services;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.Level;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.college.CollegeService;
import com.project.server.db.services.department.DepartmentService;
import com.project.server.db.services.level.LevelService;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerStartUpMgtService implements CommandLineRunner {
    private final LevelService levelService;
    private final CollegeMgtService collegeMgtService;
    private final DepartmentMgtService departmentMgtService;
    private final CollegeService collegeService;
    private final DepartmentService departmentService;
    private final AppUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createLevels();
        createAdmin();
    }

    private void createLevels(){
        if(!levelService.isExistingLevel("100"))levelService.save(toLevel("100"));
        if(!levelService.isExistingLevel("200"))levelService.save(toLevel("200"));
        if(!levelService.isExistingLevel("300"))levelService.save(toLevel("300"));
        if(!levelService.isExistingLevel("400"))levelService.save(toLevel("400"));
        if(!levelService.isExistingLevel("500"))levelService.save(toLevel("500"));

        log.info("Levels added!");
    }

    @SneakyThrows
    private void createAdmin(){
        if(!userService.isExistingAdmin()) userService.save(toAdmin());

        log.info("Admin Created!");
    }

    private Level toLevel(String level){
        return Level.builder().level(level).build();
    }

    private AppUser toAdmin(){
        return AppUser.builder()
                .userEmail("admin@bowen.edu.ng")
                .role(Role.ADMINISTRATOR)
                .userId("BU22ADMIN2001")
                .password(passwordEncoder.encode("beatsbydan"))
                .build();
    }
}
