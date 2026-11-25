package edu.orgresource.extended.rest;

import java.util.UUID;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.service.InventoryService;
import edu.orgresource.extended.service.ProjectWorkComponent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component
@Path("/v2/orgs/{organizationId}")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectWorkResource {
    @Autowired ProjectWorkComponent work;
    @Autowired C1RequestValidator validator;

    @GET @Path("/projects")
    public Response projects(@PathParam("organizationId") UUID org, @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("cursor") String cursor, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, work.projectPage(org, limit, cursor), request, true);
    }

    @POST @Path("/projects/{projectId}/update") @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProject(@PathParam("organizationId") UUID org, @PathParam("projectId") UUID id,
            @HeaderParam("If-Match") String ifMatch, ProjectFields body, @Context HttpServletRequest request) {
        validator.validate(body);
        var updated = work.updateProject(org, id, ifMatch, body);
        return versioned(200, updated, updated.version(), request);
    }

    @POST @Path("/projects/{projectId}/delete")
    public Response deleteProject(@PathParam("organizationId") UUID org, @PathParam("projectId") UUID id,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, work.deleteProject(org, id, ifMatch), request, true);
    }

    @GET @Path("/requirements")
    public Response requirements(@PathParam("organizationId") UUID org, @QueryParam("projectId") UUID project,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, work.requirementPage(org, project, limit, cursor), request, true);
    }

    @GET @Path("/requirements/{requirementId}")
    public Response requirement(@PathParam("organizationId") UUID org, @PathParam("requirementId") UUID id,
            @Context HttpServletRequest request) {
        var item = work.requirement(org, id);
        return versioned(200, item, item.version(), request);
    }

    @POST @Path("/requirements") @Consumes(MediaType.APPLICATION_JSON)
    public Response createRequirement(@PathParam("organizationId") UUID org, CreateRequirement body,
            @Context HttpServletRequest request) {
        validator.validate(body);
        var item = work.createRequirement(org, body.projectId, body.resourceId, body.quantity);
        return versioned(201, item, item.version(), request);
    }

    @POST @Path("/requirements/{requirementId}/update") @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRequirement(@PathParam("organizationId") UUID org, @PathParam("requirementId") UUID id,
            @HeaderParam("If-Match") String ifMatch, QuantityUpdate body, @Context HttpServletRequest request) {
        validator.validate(body);
        var item = work.updateRequirement(org, id, ifMatch, body.quantity);
        return versioned(200, item, item.version(), request);
    }

    @POST @Path("/requirements/{requirementId}/delete")
    public Response deleteRequirement(@PathParam("organizationId") UUID org, @PathParam("requirementId") UUID id,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, work.deleteRequirement(org, id, ifMatch), request, true);
    }

    private static Response versioned(int status, Object data, long version, HttpServletRequest request) {
        return Response.fromResponse(C1HttpResponses.envelope(status, data, request, true))
                .header("ETag", InventoryService.etag(version)).build();
    }

    public static final class ProjectFields extends ApiDtos.ProjectUpdate {
        @JsonAnySetter public void unknown(String field, JsonNode ignored) {
            throw new IllegalArgumentException("Unknown Project field.");
        }
    }

    public static class QuantityUpdate extends ApiDtos.RequirementUpdate {
        @JsonAnySetter public void unknown(String field, JsonNode ignored) {
            throw new IllegalArgumentException("Unknown Requirement field.");
        }
    }

    public static final class CreateRequirement extends QuantityUpdate {
        @NotNull public UUID projectId;
        @NotNull public UUID resourceId;
    }
}
