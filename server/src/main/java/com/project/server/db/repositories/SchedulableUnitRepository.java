package com.project.server.db.repositories;

import com.project.server.db.models.SchedulableUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulableUnitRepository extends JpaRepository<SchedulableUnit, Long> {
    Optional<SchedulableUnit> findByCode(String code);
    Optional<SchedulableUnit> findByTitle(String title);

    @Query("SELECT s FROM SchedulableUnit s LEFT JOIN FETCH s.participants WHERE s.code = :code")
    Optional<SchedulableUnit> findByCodeWithParticipants(@Param("code") String code);

    @Query("SELECT s FROM SchedulableUnit s WHERE s.general = true")
    List<SchedulableUnit> findAllGeneral();

    @Query("""
        SELECT DISTINCT s FROM SchedulableUnit s
        LEFT JOIN s.participants p
        LEFT JOIN p.level l
        LEFT JOIN p.orgUnits o
        WHERE s.unit = :unit
        AND (:orgUnitName IS NULL OR o.name = :orgUnitName)
        AND (:levelCode IS NULL OR l.code = :levelCode)
        AND s.shared = :shared
        AND s.batched = :batched
        AND s.general = :general
    """)
    List<SchedulableUnit> findAllByFilterParams(
            @Param("unit") int unit,
            @Param("orgUnitName") String orgUnitName,
            @Param("levelCode") String levelCode,
            @Param("shared") boolean shared,
            @Param("batched") boolean batched,
            @Param("general") boolean general
    );

    @Query("""
        SELECT DISTINCT s FROM SchedulableUnit s
        LEFT JOIN s.codes c
        WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :key, '%'))
        OR LOWER(c.unitCode) LIKE LOWER(CONCAT('%', :key, '%'))
    """)
    List<SchedulableUnit> filterByKey(@Param("key") String key);
}
