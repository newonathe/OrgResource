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
import javax.ws.rs.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.components.ProjectComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateProject;
import app.dto.ApiDtos.ProjectView;
import app.dto.ApiDtos.UpdateProject;

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

    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, UpdateProject request) {
        requestValidation.validate(request);
        return Response.ok(component.update(id, request.name, request.startAt, request.endAt)).build();
    }

    @POST
    @Path("/{id}/delete")
    public Response delete(@PathParam("id") Long id) {
        return Response.ok(component.delete(id)).build();
    }
}
