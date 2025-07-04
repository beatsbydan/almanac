package com.project.server.db.services.generationTracker;

import com.project.server.db.models.GenerationTracker;
import com.project.server.db.repositories.GenerationTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenerationTrackerServiceImpl implements GenerationTrackerService {
    private final GenerationTrackerRepository repository;
    @Override
    public GenerationTracker save(GenerationTracker tracker) {
        return repository.save(tracker);
    }
}
