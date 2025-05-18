package com.project.server.db.services.level;

import com.project.server.db.models.Level;
import com.project.server.db.repositories.LevelRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LevelServiceImpl implements LevelService {
    private final LevelRepository repository;

    @Override
    public boolean isExistingLevel(String level) {
        return repository.findByLevel(level).isPresent();
    }

    @Override
    public Level getByLevel(String level) throws NotFoundException {
        return repository.findByLevel(level).orElseThrow(()-> new NotFoundException(String.format("Level: '%s' was not found", level)));
    }

    @Override
    public Optional<Level> getByLevelOpt(String level) {
        return repository.findByLevel(level);
    }

    @Override
    public Level save(Level level) {
        repository.save(level);
        return level;
    }
}
