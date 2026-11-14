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
import app.dto.ApiDtos.MessageView;

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

    @POST
    @Path("/{id}/approve")
    public Response approve(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.approve(id)).build();
    }

    @POST
    @Path("/{id}/issue")
    public Response issue(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.issue(id)).build();
    }

    @POST
    @Path("/{id}/return")
    public Response returnItems(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.returnItems(id)).build();
    }

    @POST
    @Path("/{id}/consume")
    public Response consume(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.consume(id)).build();
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancel(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.cancel(id)).build();
    }

    @POST
    @Path("/{id}/delete")
    public Response delete(@javax.ws.rs.PathParam("id") Long id) {
        MessageView result = component.delete(id);
        return Response.ok(result).build();
    }

    @POST
    @Path("/{id}/revoke-sms-consent")
    public Response revokeSmsConsent(@javax.ws.rs.PathParam("id") Long id) {
        return Response.ok(component.revokeSmsConsent(id)).build();
    }
}
