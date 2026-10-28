package com.project.server;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.services.tenant.TenantService;
import com.project.server.util.VariableUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
public abstract class BaseIntegrationTest {
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TenantService tenantService;

    /**
     * Service-layer tests call services directly, bypassing TenantResolutionFilter entirely, so
     * without this every tenant-scoped query would fail with no tenant resolvable. MockMvc-based
     * controller tests DO go through the real filter chain and will redundantly (harmlessly)
     * re-set the same tenant for the duration of that one request.
     */
    @BeforeEach
    @SneakyThrows
    protected void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        TenantContext.set(tenantService.getBySlug(VariableUtil.DEFAULT_TENANT_SLUG).getId());
    }

    @AfterEach
    protected void tearDown(){
        TenantContext.clear();
    }
}
