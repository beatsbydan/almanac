package com.project.server.db.repositories;

import com.project.server.db.models.AppUser;
import com.project.server.enums.Role;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUserId(String userId);

    @Query("SELECT u FROM AppUser u WHERE u.userEmail = :email OR u.userId =:id")
    Optional<AppUser> findByEmailOrId(@Param("email") String userEmail, @Param(("id")) String userId);

    @Query("SELECT u FROM AppUser u WHERE u.level.level = :level AND u.orgUnit.name = :department")
    Optional<AppUser> hasExistingCourseRepresentative(@Param("department") String department, @Param("level") String level);

    @Query("SELECT u FROM AppUser u WHERE u.role = :role")
    Optional<AppUser> isExistingAdmin(@Param("role") Role role);
}
