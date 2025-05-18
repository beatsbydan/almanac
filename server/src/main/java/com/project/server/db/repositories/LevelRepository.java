package com.project.server.db.repositories;

import com.project.server.db.models.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByLevel(String level);
}
