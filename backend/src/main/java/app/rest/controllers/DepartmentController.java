package app.rest.controllers;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.components.DepartmentComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateDepartment;
import app.dto.ApiDtos.DepartmentView;

@Component
@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
public class DepartmentController {
    @Autowired private DepartmentComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateDepartment request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(request.name)).build();
    }

    @GET
    public List<DepartmentView> list() {
        return component.list();
    }

    @POST
    @Path("/{id}/delete")
    public Response delete(@PathParam("id") Long id) {
        return Response.ok(component.delete(id)).build();
    }
}
