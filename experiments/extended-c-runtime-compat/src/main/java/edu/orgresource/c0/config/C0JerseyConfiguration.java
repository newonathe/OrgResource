package edu.orgresource.c0.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.orgresource.c0.api.C0ApiExceptionMapper;
import edu.orgresource.c0.api.C0FallbackExceptionMapper;
import edu.orgresource.c0.api.C0IntegrityExceptionMapper;
import edu.orgresource.c0.api.C0ValidationExceptionMapper;
import edu.orgresource.c0.rest.AllocationResource;
import edu.orgresource.c0.rest.DepartmentResource;
import edu.orgresource.c0.rest.ProjectResource;
import edu.orgresource.c0.rest.ResourceRequirementResource;
import edu.orgresource.c0.rest.ResourceResource;
import edu.orgresource.c0.rest.V2DepartmentResource;

@Configuration
public class C0JerseyConfiguration {
    @Bean
    ResourceConfig c0ResourceConfig() {
        ResourceConfig config = new ResourceConfig();
        config.register(DepartmentResource.class);
        config.register(ProjectResource.class);
        config.register(ResourceResource.class);
        config.register(ResourceRequirementResource.class);
        config.register(AllocationResource.class);
        config.register(V2DepartmentResource.class);
        config.register(JacksonFeature.class);
        config.register(C0ApiExceptionMapper.class);
        config.register(C0IntegrityExceptionMapper.class);
        config.register(C0ValidationExceptionMapper.class);
        config.register(C0FallbackExceptionMapper.class);
        return config;
    }
}
