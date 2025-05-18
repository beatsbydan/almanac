package com.project.server.db.repositories;

import com.project.server.db.models.CourseCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCodeRepository extends JpaRepository<CourseCode, Long> {
    Optional<CourseCode> findByCourseCode(String courseCode);
    @Transactional
    @Modifying
    @Query("DELETE FROM CourseCode c WHERE c.course.code = :code")
    void deleteAllByCourseCode(@Param("code") String courseCode);
    @Query("SELECT c FROM CourseCode c WHERE c.course.code = :code")
    List<CourseCode> findByCourse(@Param("code") String code);
}
