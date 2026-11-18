package edu.orgresource.extended.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.orgresource.extended.api.C1ApiExceptionMapper;
import edu.orgresource.extended.api.C1BadRequestExceptionMapper;
import edu.orgresource.extended.api.C1ClientErrorResponseFilter;
import edu.orgresource.extended.api.C1FallbackExceptionMapper;
import edu.orgresource.extended.api.C1IntegrityExceptionMapper;
import edu.orgresource.extended.api.C1JacksonInputExceptionMapper;
import edu.orgresource.extended.api.C1ParamExceptionMapper;
import edu.orgresource.extended.api.C1ValidationExceptionMapper;
import edu.orgresource.extended.rest.AllocationResource;
import edu.orgresource.extended.rest.C1AuthResource;
import edu.orgresource.extended.rest.C1GovernanceResource;
import edu.orgresource.extended.rest.C1InvitationResource;
import edu.orgresource.extended.rest.C1SessionResource;
import edu.orgresource.extended.rest.DepartmentResource;
import edu.orgresource.extended.rest.InventoryResource;
import edu.orgresource.extended.rest.ProjectResource;
import edu.orgresource.extended.rest.ReportingResource;
import edu.orgresource.extended.rest.ResourceRequirementResource;
import edu.orgresource.extended.rest.ResourceResource;
import edu.orgresource.extended.rest.V2DepartmentResource;

@Configuration
public class C1JerseyConfiguration {
    @Bean
    ResourceConfig c1ResourceConfig() {
        ResourceConfig config = new ResourceConfig();
        config.register(DepartmentResource.class);
        config.register(ProjectResource.class);
        config.register(ResourceResource.class);
        config.register(ResourceRequirementResource.class);
        config.register(AllocationResource.class);
        config.register(V2DepartmentResource.class);
        config.register(C1AuthResource.class);
        config.register(C1SessionResource.class);
        config.register(C1GovernanceResource.class);
        config.register(C1InvitationResource.class);
        config.register(InventoryResource.class);
        config.register(ReportingResource.class);
        config.register(JacksonFeature.class);
        config.register(MultiPartFeature.class);
        config.register(C1ApiExceptionMapper.class);
        config.register(C1BadRequestExceptionMapper.class);
        config.register(C1IntegrityExceptionMapper.class);
        config.register(C1JacksonInputExceptionMapper.class);
        config.register(C1ParamExceptionMapper.class);
        config.register(C1ClientErrorResponseFilter.class);
        config.register(C1ValidationExceptionMapper.class);
        config.register(C1FallbackExceptionMapper.class);
        return config;
    }
}
