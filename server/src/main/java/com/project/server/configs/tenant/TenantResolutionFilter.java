package com.project.server.configs.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.db.models.Tenant;
import com.project.server.db.services.tenant.TenantService;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.NotFoundException;
import com.project.server.util.VariableUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the tenant for every request — the one piece that runs before anything else, so even
 * public auth endpoints are tenant-scoped. Resolution is header-based ({@code X-Tenant-Slug}) for
 * now: a deliberately simple placeholder for whatever subdomain/branding strategy the client
 * eventually uses, chosen so this backend change doesn't force an unrelated client rewrite yet.
 * Requests with no header fall back to the single legacy tenant, so existing single-tenant
 * traffic (and every existing test) keeps working unmodified.
 */
@Configuration
@RequiredArgsConstructor
public class TenantResolutionFilter extends OncePerRequestFilter {
    private final TenantService tenantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String slug = request.getHeader(VariableUtil.TENANT_SLUG_HEADER);
        if(slug == null || slug.isBlank()) slug = VariableUtil.DEFAULT_TENANT_SLUG;

        Tenant tenant;
        try {
            tenant = tenantService.getBySlug(slug);
        }
        catch(NotFoundException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(new MessageResponse("Unknown tenant.")));
            response.getWriter().flush();
            return;
        }

        try {
            TenantContext.set(tenant.getId());
            filterChain.doFilter(request, response);
        }
        finally {
            TenantContext.clear();
        }
    }
}
