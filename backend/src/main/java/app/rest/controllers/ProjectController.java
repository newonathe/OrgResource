package app.rest.controllers;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.components.ProjectComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateProject;
import app.dto.ApiDtos.ProjectView;

@Component
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    @Autowired private ProjectComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateProject request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(request.departmentId,
                request.name, request.startAt, request.endAt)).build();
    }

    @GET
    public List<ProjectView> list(@QueryParam("departmentId") Long departmentId) {
        return component.list(departmentId);
    }
}
