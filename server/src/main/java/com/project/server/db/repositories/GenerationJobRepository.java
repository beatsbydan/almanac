package com.project.server.db.repositories;

import com.project.server.db.models.GenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenerationJobRepository extends JpaRepository<GenerationJob, Long> {
    Optional<GenerationJob> findByCode(String code);
}
