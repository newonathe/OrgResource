package app.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import app.rest.controllers.AllocationController;
import app.rest.controllers.DepartmentController;
import app.rest.controllers.ProjectController;
import app.rest.controllers.ResourceController;
import app.rest.controllers.RequirementController;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(DepartmentController.class);
        register(ProjectController.class);
        register(ResourceController.class);
        register(RequirementController.class);
        register(AllocationController.class);
        register(ApiExceptionMapper.class);
        register(JacksonFeature.class);
        register(MultiPartFeature.class);
    }
}
