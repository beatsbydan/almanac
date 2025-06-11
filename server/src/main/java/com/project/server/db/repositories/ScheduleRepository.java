package com.project.server.db.repositories;

import com.project.server.db.models.CourseParticipant;
import com.project.server.db.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findByCode(String code);
    @Query("""
            SELECT DISTINCT s FROM Schedule s
            JOIN s.course c
            JOIN c.courseParticipants cp
            WHERE :startTime < s.endTime
            AND :endTime > s.startTime
            AND cp IN :courseParticipants
            """)
    List<Schedule> findConflictingSchedules(
            @Param("startTime") int startTime,
            @Param("endTime") int endTime,
            @Param("courseParticipants") List<CourseParticipant> courseParticipants
    );
    @Query("""
            SELECT DISTINCT s FROM Schedule s
            LEFT JOIN s.course c
            LEFT JOIN c.courseParticipants cp
            LEFT JOIN cp.level l
            LEFT JOIN cp.departments d
            WHERE l.level = :level
            AND d.name = :department
            """)
    List<Schedule> findByCourseRepresentative(
            @Param("level") String level,
            @Param("department") String department);

    @Query("SELECT s FROM Schedule s WHERE s.course.courseTitle = :title")
    Schedule findByCourse(@Param("title") String courseTitle);
    @Query("SELECT s FROM Schedule s WHERE s.appUser.userId = :userId")
    List<Schedule> findByLecturer(@Param("userId") String userId);

    @Transactional
    @Modifying
    void deleteByCode(String code);
}