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
import app.components.RequirementComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateRequirement;
import app.dto.ApiDtos.RequirementView;

@Component
@Path("/requirements")
@Produces(MediaType.APPLICATION_JSON)
public class RequirementController {
    @Autowired private RequirementComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateRequirement request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(
                request.projectId, request.resourceId, request.quantity)).build();
    }

    @GET
    public List<RequirementView> list(@QueryParam("projectId") Long projectId) {
        return component.list(projectId);
    }
}
