package com.project.server.db.repositories;

import com.project.server.db.models.GenerationTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationTrackerRepository extends JpaRepository<GenerationTracker, Long> {
}
