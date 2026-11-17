package edu.orgresource.extended.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.service.C1Component;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/departments") @Produces(MediaType.APPLICATION_JSON)
public class DepartmentResource {
    @Autowired private C1Component component;
    @Autowired private C1RequestValidator requestValidator;
    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ApiDtos.CreateDepartment request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createDepartment(request)).build();
    }
    @POST @Path("/{id}/update") @Consumes(MediaType.APPLICATION_JSON)
    public ApiDtos.DepartmentView update(@PathParam("id") Long id, ApiDtos.DepartmentUpdate request) {
        requestValidator.validate(request);
        return component.updateDepartment(id, request);
    }
    @POST @Path("/{id}/delete")
    public ApiDtos.MessageView delete(@PathParam("id") Long id) { return component.deleteDepartment(id); }
    @GET public List<ApiDtos.DepartmentView> list() { return component.listDepartments(); }
}
