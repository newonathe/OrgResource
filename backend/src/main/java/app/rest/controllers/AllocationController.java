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
import app.components.AllocationComponent;
import app.components.RequestValidation;
import app.dto.ApiDtos.AllocationView;
import app.dto.ApiDtos.CreateAllocation;
import app.entities.AllocationStatus;

@Component
@Path("/allocations")
@Produces(MediaType.APPLICATION_JSON)
public class AllocationController {
    @Autowired private AllocationComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateAllocation request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(request.requirementId,
                request.quantity, request.borrowerName, request.phoneNumber, request.smsConsent)).build();
    }

    @GET
    public List<AllocationView> list(@QueryParam("requirementId") Long requirementId,
                                     @QueryParam("status") AllocationStatus status) {
        return component.list(requirementId, status);
    }
}
