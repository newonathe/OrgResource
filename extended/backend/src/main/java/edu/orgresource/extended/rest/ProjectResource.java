package edu.orgresource.extended.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.service.C1Component;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/projects") @Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {
    @Autowired private C1Component component;
    @Autowired private C1RequestValidator requestValidator;
    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ApiDtos.CreateProject request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createProject(request)).build();
    }
    @GET public List<ApiDtos.ProjectView> list(@QueryParam("departmentId") Long departmentId) {
        return component.listProjects(departmentId);
    }
}
