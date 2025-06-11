package com.project.server.db.repositories;

import com.project.server.db.models.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    Optional<ClassRoom> findByName(String name);
    Optional<ClassRoom> findByCode(String code);

    @Query("SELECT c FROM ClassRoom c WHERE c.college.name = :college OR c.college.shortName = :college")
    List<ClassRoom> findAllByCollege(@Param("college") String college);

    @Query("SELECT c FROM ClassRoom c WHERE c.college.shortName = :college")
    List<ClassRoom> filterByCollege(@Param("college") String college);

    @Query("SELECT c FROM ClassRoom c WHERE c.college IS NULL")
    List<ClassRoom> findAllWithoutCollege();

    @Query("""
        SELECT c FROM ClassRoom c
        LEFT JOIN c.college col
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :key, '%'))
        OR LOWER(col.name) LIKE LOWER(CONCAT('%', :key, '%'))
        OR LOWER(col.shortName) LIKE LOWER(CONCAT('%', :key, '%'))
    """)
    List<ClassRoom> filterByNameOrCollege(@Param("key") String key);
}
