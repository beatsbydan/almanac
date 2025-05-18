package com.project.server.db.services.level;

import com.project.server.db.models.Level;
import com.project.server.exceptions.NotFoundException;

import java.util.Optional;

public interface LevelService {
    boolean isExistingLevel(String level);
    Level getByLevel(String level) throws NotFoundException;
    Optional<Level> getByLevelOpt(String level);
    Level save(Level level);
}
