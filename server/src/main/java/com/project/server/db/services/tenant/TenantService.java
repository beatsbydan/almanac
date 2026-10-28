package com.project.server.db.services.tenant;

import com.project.server.db.models.Tenant;
import com.project.server.exceptions.NotFoundException;

public interface TenantService {
    Tenant getBySlug(String slug) throws NotFoundException;
    Tenant getById(Long id) throws NotFoundException;
    Tenant save(Tenant tenant);
}
