package com.project.server.db.repositories;

import com.project.server.db.models.SchedulableUnitParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulableUnitParticipantRepository extends JpaRepository<SchedulableUnitParticipant, Long> {
    @Query("SELECT p FROM SchedulableUnitParticipant p WHERE p.schedulableUnit.code = :code")
    List<SchedulableUnitParticipant> findByUnit(@Param("code") String unitCode);
}
