package com.project.server.db.services.generationJob;

import com.project.server.db.models.GenerationJob;
import com.project.server.exceptions.NotFoundException;

public interface GenerationJobService {
    GenerationJob save(GenerationJob job);
    GenerationJob getByCode(String code) throws NotFoundException;
}
