package com.project.server.db.repositories;

import com.project.server.db.models.TimeTable;
import com.project.server.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {
    Optional<TimeTable> findByCode(String code);
    @Query("SELECT t FROM TimeTable t WHERE t.active = true")
    Optional<TimeTable> findActiveTimetable();
    @Query("SELECT t FROM TimeTable t WHERE t.session = :session AND t.semester = :semester")
    Optional<TimeTable> findBySessionAndSemester(@Param("session") String session, @Param("semester")Semester semester);
}
