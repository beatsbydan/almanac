package com.project.server.configs.tenant;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantHibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer tenantIdentifierResolverCustomizer() {
        CurrentTenantResolver resolver = new CurrentTenantResolver();
        return hibernateProperties -> hibernateProperties.put("hibernate.tenant_identifier_resolver", resolver);
    }
}
