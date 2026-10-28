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

    @Query("""
            SELECT c FROM ClassRoom c
            LEFT JOIN FETCH c.orgUnit o
            LEFT JOIN FETCH o.children ch
            WHERE o.name = :orgUnit
            OR o.shortName = :orgUnit
    """)
    List<ClassRoom> findAllByCollege(@Param("orgUnit") String orgUnit);

    @Query("SELECT c FROM ClassRoom c WHERE c.orgUnit.name = :orgUnit OR c.orgUnit.shortName = :orgUnit")
    List<ClassRoom> filterByCollege(@Param("orgUnit") String orgUnit);

    @Query("SELECT c FROM ClassRoom c WHERE c.orgUnit IS NULL")
    List<ClassRoom> findAllWithoutCollege();

    @Query("""
        SELECT c FROM ClassRoom c
        LEFT JOIN c.orgUnit o
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :key, '%'))
        OR LOWER(o.name) LIKE LOWER(CONCAT('%', :key, '%'))
        OR LOWER(o.shortName) LIKE LOWER(CONCAT('%', :key, '%'))
    """)
    List<ClassRoom> filterByNameOrCollege(@Param("key") String key);
}
