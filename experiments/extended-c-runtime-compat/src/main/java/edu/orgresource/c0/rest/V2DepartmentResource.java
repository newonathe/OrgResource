package edu.orgresource.c0.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.web.csrf.CsrfToken;
import edu.orgresource.c0.api.ApiDtos;
import edu.orgresource.c0.api.C0RequestValidator;
import edu.orgresource.c0.service.C0Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/v2") @Produces(MediaType.APPLICATION_JSON)
public class V2DepartmentResource {
    @Autowired private C0Component component;
    @Autowired private C0RequestValidator requestValidator;
    @GET @Path("/csrf")
    public ApiDtos.CsrfView csrf(@Context HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) token = (CsrfToken) request.getAttribute("_csrf");
        return new ApiDtos.CsrfView(token == null ? null : token.getToken());
    }
    @GET @Path("/departments/{id}")
    public ApiDtos.DepartmentView getDepartment(@PathParam("id") Long id) {
        return component.readDepartmentV2(id);
    }
    @POST @Path("/departments") @Consumes(MediaType.APPLICATION_JSON)
    public Response createDepartment(ApiDtos.CreateDepartment request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createDepartmentV2(request)).build();
    }
}
