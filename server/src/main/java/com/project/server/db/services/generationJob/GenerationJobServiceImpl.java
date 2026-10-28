package com.project.server.db.services.generationJob;

import com.project.server.db.models.GenerationJob;
import com.project.server.db.repositories.GenerationJobRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationJobServiceImpl implements GenerationJobService {
    private final GenerationJobRepository repository;

    @Override
    public GenerationJob save(GenerationJob job) {
        return repository.save(job);
    }

    @Override
    public GenerationJob getByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(() -> new NotFoundException("This generation job does not exist."));
    }
}
