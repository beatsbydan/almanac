package com.project.server.db.repositories;

import com.project.server.db.models.Course;
import com.project.server.db.models.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseTitle(String courseTitle);
    Optional<Course> findByCode(String code);
    @Query("""
        SELECT c FROM Course c
        JOIN c.courseCodes cc
        WHERE cc.courseCode = :code
    """)
    Optional<Course> findByCourseCode(@Param("code") String courseCode);
    @Query("SELECT c FROM Course c WHERE c.general = true")
    List<Course> findAllUniversityWideCourses();
    @Query("""
    SELECT DISTINCT c FROM Course c
    LEFT JOIN c.courseParticipants cp
    LEFT JOIN cp.level l
    LEFT JOIN cp.departments d
    WHERE c.courseUnit = :unit
    AND (:department IS NULL OR d.name = :department)
    AND (:levelCode IS NULL OR l.code = :levelCode)
    AND c.shared = :shared
    AND c.batched = :batched
    AND c.general = :general
    """)
    List<Course> findAllByFilterParams(
            @Param("unit") int unit,
            @Param("department") String department,
            @Param("levelCode") String levelCode,
            @Param("shared") boolean shared,
            @Param("batched") boolean batched,
            @Param("general") boolean general
    );

    @Query("""
    SELECT DISTINCT c FROM Course c
    LEFT JOIN c.courseCodes cc
    WHERE LOWER(cc.courseCode) LIKE LOWER(CONCAT('%', :key, '%'))
    """)
    List<Course> filterCourses(@Param("key") String key);
}
