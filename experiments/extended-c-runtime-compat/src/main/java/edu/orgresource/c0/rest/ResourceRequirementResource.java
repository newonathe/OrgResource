package edu.orgresource.c0.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.orgresource.c0.api.ApiDtos;
import edu.orgresource.c0.api.C0RequestValidator;
import edu.orgresource.c0.service.C0Component;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/requirements") @Produces(MediaType.APPLICATION_JSON)
public class ResourceRequirementResource {
    @Autowired private C0Component component;
    @Autowired private C0RequestValidator requestValidator;
    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ApiDtos.CreateRequirement request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createRequirement(request)).build();
    }
    @GET public List<ApiDtos.RequirementView> list(@QueryParam("projectId") Long projectId) {
        return component.listRequirements(projectId);
    }
}
