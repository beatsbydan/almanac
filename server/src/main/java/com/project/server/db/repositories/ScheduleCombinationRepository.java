package com.project.server.db.repositories;

import com.project.server.db.models.ScheduleCombination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleCombinationRepository extends JpaRepository<ScheduleCombination, Long> {
    @Query("""
        SELECT s FROM ScheduleCombination s
        WHERE s.day = :day
        AND :startTime < s.endTime
        AND :endTime > s.startTime
        AND s.classroomName = :className
        AND s.schedule.timeTable.code = :timetableCode
    """)
    Optional<ScheduleCombination> findByCombination(
            @Param("day") String day,
            @Param("startTime") int startTime,
            @Param("endTime") int endTime,
            @Param("className") String className,
            @Param("timetableCode") String timetableCode
    );
    @Query("SELECT s FROM ScheduleCombination s WHERE s.schedule.code = :code")
    List<ScheduleCombination> findBySchedule(@Param("code") String code);
    @Transactional
    @Modifying
    @Query("DELETE FROM ScheduleCombination s WHERE s.schedule.code = :code")
    void deleteAllBySchedule(@Param("code") String scheduleCode);
    @Query("""
            SELECT s FROM ScheduleCombination s
            WHERE (:day IS NULL OR s.day = :day)
            AND (:userId IS NULL OR s.schedule.appUser.userId = :userId)
            AND (:timetableCode IS NULL OR s.schedule.timeTable.code = :timetableCode)
            """)
    List<ScheduleCombination> findByFilterParams(
            @Param("userId") String userId,
            @Param("day") String day,
            @Param("timetableCode") String timetableCode);

    @Query("SELECT s FROM ScheduleCombination s WHERE s.schedule.timeTable.code = :code")
    Page<ScheduleCombination> getSchedules(@Param("code") String timetableCode, Pageable pageable);

    @Query("SELECT s FROM ScheduleCombination s WHERE s.schedule.course.code = :code")
    Optional<ScheduleCombination> hasScheduleForCourse( @Param("code") String courseCode);
}