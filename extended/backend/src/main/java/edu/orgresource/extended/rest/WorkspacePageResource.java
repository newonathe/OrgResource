package edu.orgresource.extended.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.domain.ResourceType;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.service.C1Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component
@Path("/v2/workspace")
@Produces(MediaType.APPLICATION_JSON)
public class WorkspacePageResource {
    @Autowired private C1Component component;

    @GET @Path("/overview")
    public Response overview(@Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.workspaceOverview(), request, true);
    }

    @GET @Path("/departments")
    public Response departments(@QueryParam("search") String search,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.departmentPage(search, limit, cursor), request, true);
    }

    @GET @Path("/projects")
    public Response projects(@QueryParam("departmentId") Long departmentId,
            @QueryParam("search") String search, @QueryParam("period") String period,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.projectPage(departmentId, search, period, limit, cursor), request, true);
    }

    @GET @Path("/resources")
    public Response resources(@QueryParam("type") ResourceType type, @QueryParam("search") String search,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.resourcePage(type, search, limit, cursor), request, true);
    }

    @GET @Path("/requirements")
    public Response requirements(@QueryParam("projectId") Long projectId,
            @QueryParam("search") String search,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.requirementPage(projectId, search, limit, cursor), request, true);
    }

    @GET @Path("/allocations")
    public Response allocations(@QueryParam("requirementId") Long requirementId, @QueryParam("projectId") Long projectId,
            @QueryParam("status") AllocationStatus status,
            @DefaultValue("50") @QueryParam("limit") int limit, @QueryParam("cursor") String cursor,
            @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, component.allocationPage(requirementId, projectId, status, limit, cursor), request, true);
    }
}
