package edu.orgresource.extended.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.service.C1Component;
import edu.orgresource.extended.security.C1OrganizationContext;
import edu.orgresource.extended.domain.AllocationStatus;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/allocations") @Produces(MediaType.APPLICATION_JSON)
public class AllocationResource {
    @Autowired private C1Component component;
    @Autowired private C1RequestValidator requestValidator;
    @Autowired private C1OrganizationContext organizationContext;
    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ApiDtos.CreateAllocation request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createAllocation(request)).build();
    }
    @GET public List<ApiDtos.AllocationView> list(@QueryParam("requirementId") Long requirementId,
            @QueryParam("status") AllocationStatus status) {
        return component.listAllocations(requirementId, status);
    }

    @POST @Path("/{id}/approve")
    public ApiDtos.AllocationView approve(@PathParam("id") Long id) {
        return component.approve(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/issue")
    public ApiDtos.AllocationView issue(@PathParam("id") Long id) {
        return component.issue(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/return")
    public ApiDtos.AllocationView returnAllocation(@PathParam("id") Long id) {
        return component.returnAllocation(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/consume")
    public ApiDtos.AllocationView consume(@PathParam("id") Long id) {
        return component.consume(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/cancel")
    public ApiDtos.AllocationView cancel(@PathParam("id") Long id) {
        return component.cancel(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/delete")
    public ApiDtos.MessageView delete(@PathParam("id") Long id) {
        return component.deleteAllocation(id, organizationContext.currentOrganizationId());
    }

    @POST @Path("/{id}/revoke-sms-consent")
    public ApiDtos.AllocationView revokeSmsConsent(@PathParam("id") Long id) {
        return component.revokeSmsConsent(id, organizationContext.currentOrganizationId());
    }
}
