package com.project.server.db.repositories;

import com.project.server.db.models.CourseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, Long> {
    @Query("SELECT c FROM CourseParticipant c WHERE c.course.code = :code")
    List<CourseParticipant> findByCourse(@Param("code") String courseCode);

    @Transactional
    @Modifying
    @Query("DELETE FROM CourseParticipant c WHERE c.course.code = :code")
    void deleteAllByCourse(@Param("code") String courseCode);
}