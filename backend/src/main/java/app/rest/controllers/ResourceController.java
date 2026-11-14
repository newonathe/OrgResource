package app.rest.controllers;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.components.ResourceComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateResource;
import app.dto.ApiDtos.ResourceAvailabilityView;
import app.dto.ApiDtos.ResourceView;
import app.entities.ResourceType;

@Component
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourceController {
    @Autowired private ResourceComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateResource request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(request.name,
                request.type, request.totalQuantity)).build();
    }

    @GET
    public List<ResourceView> list(@QueryParam("type") ResourceType type) {
        return component.list(type);
    }

    @GET
    @Path("/{id}/availability")
    public ResourceAvailabilityView availability(@PathParam("id") Long id) {
        return component.getAvailability(id);
    }
}
