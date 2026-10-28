package com.project.server.db.services.tenant;

import com.project.server.db.models.Tenant;
import com.project.server.db.repositories.TenantRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {
    private final TenantRepository repository;

    @Override
    public Tenant getBySlug(String slug) throws NotFoundException {
        return repository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Tenant not found."));
    }

    @Override
    public Tenant getById(Long id) throws NotFoundException {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Tenant not found."));
    }

    @Override
    public Tenant save(Tenant tenant) {
        return repository.save(tenant);
    }
}
