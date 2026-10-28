package com.project.server.db.repositories;

import com.project.server.db.models.SchedulableUnitCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulableUnitCodeRepository extends JpaRepository<SchedulableUnitCode, Long> {
    Optional<SchedulableUnitCode> findByUnitCode(String unitCode);
}
