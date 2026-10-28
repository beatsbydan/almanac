package com.project.server.db.repositories;

import com.project.server.db.models.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            LEFT JOIN s.schedulableUnit c
            LEFT JOIN c.participants cp
            WHERE :startTime < s.endTime
            AND :endTime > s.startTime
            AND s.day = :day
            AND cp.id IN :courseParticipantIds
            """)
    List<Schedule> findConflictingSchedules(
            @Param("startTime") int startTime,
            @Param("endTime") int endTime,
            @Param("courseParticipantIds") List<Long> courseParticipantIds,
            @Param("day") String day
    );
    @Query("""
            SELECT DISTINCT s FROM Schedule s
            LEFT JOIN s.schedulableUnit c
            LEFT JOIN c.participants cp
            LEFT JOIN cp.level l
            LEFT JOIN cp.orgUnits d
            WHERE l.level = :level
            AND d.name = :department
            """)
    List<Schedule> findByCourseRepresentative(
            @Param("level") String level,
            @Param("department") String department);

    @Query("SELECT s FROM Schedule s WHERE s.schedulableUnit.title = :title")
    Schedule findByCourse(@Param("title") String courseTitle);
    @Query("SELECT s FROM Schedule s WHERE s.appUser.userId = :userId")
    List<Schedule> findByLecturer(@Param("userId") String userId);

    @Query("""
        SELECT s.id FROM Schedule s
        LEFT JOIN s.classRooms cl
        LEFT JOIN cl.orgUnit org
        WHERE org.shortName = :college
        AND s.processedForGeneration = false
    """)
    Page<Long> findUnprocessedScheduleIdsByCollege(@Param("college") String college, Pageable pageable);
    @Query("""
        SELECT DISTINCT s FROM Schedule s
        LEFT JOIN FETCH s.schedulableUnit c
        LEFT JOIN FETCH s.classRooms cl
        WHERE s.id IN :ids
    """)
    List<Schedule> fetchUnprocessedSchedulesWithDetailsByCollegeByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT s.id FROM Schedule s
        LEFT JOIN s.schedulableUnit c
        WHERE c.general = true
        AND s.processedForGeneration = false
    """)
    Page<Long> findUnprocessedUniversityWideScheduleIds(Pageable pageable);
    @Query("""
        SELECT DISTINCT s FROM Schedule s
        LEFT JOIN FETCH s.schedulableUnit c
        LEFT JOIN FETCH s.classRooms cl
        WHERE s.id IN :ids
    """)
    List<Schedule> fetchUnprocessedUniversityWideSchedulesByIds(@Param("ids") List<Long> ids);

    @Transactional
    @Modifying
    void deleteByCode(String code);
}
