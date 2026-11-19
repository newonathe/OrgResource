package edu.orgresource.extended.rest;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.api.InventoryDtos;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.service.InventoryService;
import edu.orgresource.extended.service.InventoryService.InventoryAction;
import edu.orgresource.extended.service.InventoryService.OperationResponse;
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
public class InventoryResource {
    @Autowired InventoryService inventory;
    @Autowired C1RequestValidator validator;
    @Autowired ObjectMapper objectMapper;

    @GET @Path("/projects/{projectId}")
    public Response projectAccess(@PathParam("organizationId") UUID organizationId,
            @PathParam("projectId") UUID projectId, @Context HttpServletRequest request) {
        var project = inventory.projectAccess(organizationId, projectId);
        return Response.fromResponse(C1HttpResponses.envelope(200, project, request, true))
                .header("ETag", "\"v" + project.version() + "\"").build();
    }

    @POST @Path("/projects/{projectId}/lead-assignment") @Consumes(MediaType.APPLICATION_JSON)
    public Response assignLead(@PathParam("organizationId") UUID organizationId,
            @PathParam("projectId") UUID projectId, JsonNode body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        if (body == null || !body.isObject() || body.size() != 2 || !body.has("leadMembershipId")
                || !body.hasNonNull("operationId"))
            throw new C1ApiException(400, "VALIDATION_FAILED", "Supply operationId and leadMembershipId. Set leadMembershipId to null to clear.");
        UUID lead = body.get("leadMembershipId").isNull() ? null : requestUuid(body.get("leadMembershipId"), "leadMembershipId");
        UUID operationId = requestUuid(body.get("operationId"), "operationId");
        String requestId = C1HttpResponses.requestId(request);
        return operation(inventory.assignProjectLead(organizationId, projectId, lead, operationId,
                idempotencyKey, ifMatch, requestId), requestId);
    }

    private static UUID requestUuid(JsonNode value, String field) {
        try {
            if (value.isTextual() && value.asText().length() == 36) return UUID.fromString(value.asText());
        } catch (IllegalArgumentException invalid) {
            // Return the same controlled error for a malformed or missing UUID.
        }
        throw new C1ApiException(400, "VALIDATION_FAILED", field + " must be a UUID.");
    }

    @POST @Path("/resources/{resourceId}/receive") @Consumes(MediaType.APPLICATION_JSON)
    public Response receive(@PathParam("organizationId") UUID organizationId,
            @PathParam("resourceId") UUID resourceId, InventoryDtos.ReceiveRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        validator.validate(body);
        String requestId = C1HttpResponses.requestId(request);
        return operation(inventory.receive(organizationId, resourceId, body, idempotencyKey, ifMatch, requestId), requestId);
    }

    @POST @Path("/resources/{resourceId}/adjust") @Consumes(MediaType.APPLICATION_JSON)
    public Response adjust(@PathParam("organizationId") UUID organizationId,
            @PathParam("resourceId") UUID resourceId, InventoryDtos.AdjustRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        validator.validate(body);
        String requestId = C1HttpResponses.requestId(request);
        return operation(inventory.adjust(organizationId, resourceId, body, idempotencyKey, ifMatch, requestId), requestId);
    }

    @GET @Path("/resources/{resourceId}/movements")
    public Response movements(@PathParam("organizationId") UUID organizationId,
            @PathParam("resourceId") UUID resourceId, @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("after") UUID after, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, inventory.movements(organizationId, resourceId, limit, after), request, true);
    }

    @GET @Path("/operations/{operationId}")
    public Response operationReceipt(@PathParam("organizationId") UUID organizationId,
            @PathParam("operationId") UUID operationId, @Context HttpServletRequest request) {
        String json = inventory.operationReceipt(organizationId, operationId, C1HttpResponses.requestId(request));
        try {
            JsonNode receipt = objectMapper.readTree(json);
            return Response.ok(receipt).type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Request-ID", C1HttpResponses.requestId(request))
                    .header("Cache-Control", "no-store").build();
        } catch (JsonProcessingException malformed) {
            throw new IllegalStateException("Stored operation receipt is not valid JSON.", malformed);
        }
    }

    @POST @Path("/allocations/{allocationId}/approve") @Consumes(MediaType.APPLICATION_JSON)
    public Response approve(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, InventoryDtos.AllocationOperationRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return transition(organizationId, allocationId, InventoryAction.APPROVE, body, idempotencyKey, ifMatch, request);
    }

    @POST @Path("/allocations/{allocationId}/issue") @Consumes(MediaType.APPLICATION_JSON)
    public Response issue(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, InventoryDtos.AllocationOperationRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return transition(organizationId, allocationId, InventoryAction.ISSUE, body, idempotencyKey, ifMatch, request);
    }

    @POST @Path("/allocations/{allocationId}/return") @Consumes(MediaType.APPLICATION_JSON)
    public Response returnAllocation(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, InventoryDtos.AllocationOperationRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return transition(organizationId, allocationId, InventoryAction.RETURN, body, idempotencyKey, ifMatch, request);
    }

    @POST @Path("/allocations/{allocationId}/consume") @Consumes(MediaType.APPLICATION_JSON)
    public Response consume(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, InventoryDtos.AllocationOperationRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return transition(organizationId, allocationId, InventoryAction.CONSUME, body, idempotencyKey, ifMatch, request);
    }

    @POST @Path("/allocations/{allocationId}/cancel") @Consumes(MediaType.APPLICATION_JSON)
    public Response cancel(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, InventoryDtos.AllocationOperationRequest body,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("If-Match") String ifMatch, @Context HttpServletRequest request) {
        return transition(organizationId, allocationId, InventoryAction.CANCEL, body, idempotencyKey, ifMatch, request);
    }

    @GET @Path("/resources/{resourceId}")
    public Response resourceState(@PathParam("organizationId") UUID organizationId,
            @PathParam("resourceId") UUID resourceId, @Context HttpServletRequest request) {
        InventoryDtos.ResourceState state = inventory.resourceState(organizationId, resourceId);
        return Response.fromResponse(C1HttpResponses.envelope(200, state, request, true))
                .header("ETag", "\"v" + state.version() + "\"").build();
    }

    @POST @Path("/resources/{resourceId}/image/delete")
    public Response removeResourceImage(@PathParam("organizationId") UUID organizationId,
            @PathParam("resourceId") UUID resourceId, @HeaderParam("If-Match") String ifMatch,
            @Context HttpServletRequest request) {
        InventoryDtos.ResourceState state = inventory.removeResourceImage(organizationId, resourceId, ifMatch);
        return Response.fromResponse(C1HttpResponses.envelope(200, state, request, true))
                .header("ETag", "\"v" + state.version() + "\"").build();
    }

    @GET @Path("/resources")
    public Response resourcePage(@PathParam("organizationId") UUID organizationId,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("after") UUID after,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, inventory.resourcePage(organizationId, limit, after), request, true);
    }

    @GET @Path("/allocations/{allocationId}")
    public Response allocationState(@PathParam("organizationId") UUID organizationId,
            @PathParam("allocationId") UUID allocationId, @Context HttpServletRequest request) {
        InventoryDtos.AllocationState state = inventory.allocationState(organizationId, allocationId);
        return Response.fromResponse(C1HttpResponses.envelope(200, state, request, true))
                .header("ETag", "\"v" + state.version() + "\"").build();
    }

    @GET @Path("/allocations")
    public Response allocationPage(@PathParam("organizationId") UUID organizationId,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("after") UUID after,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, inventory.allocationPage(organizationId, limit, after), request, true);
    }

    private Response transition(UUID organizationId, UUID allocationId, InventoryAction action,
            InventoryDtos.AllocationOperationRequest body, String idempotencyKey, String ifMatch,
            HttpServletRequest request) {
        validator.validate(body);
        String requestId = C1HttpResponses.requestId(request);
        return operation(inventory.transition(organizationId, allocationId, action, body,
                idempotencyKey, ifMatch, requestId), requestId);
    }

    private Response operation(OperationResponse operation, String requestId) {
        try {
            JsonNode body = objectMapper.readTree(operation.json());
            Response.ResponseBuilder response = Response.status(operation.status())
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Request-ID", requestId)
                    .header("Cache-Control", "no-store");
            if (operation.etag() != null) response.header("ETag", operation.etag());
            return response.build();
        } catch (JsonProcessingException malformed) {
            throw new IllegalStateException("The inventory operation response is not valid JSON.", malformed);
        }
    }
}
