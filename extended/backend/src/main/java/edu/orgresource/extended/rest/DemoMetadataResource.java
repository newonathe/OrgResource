package edu.orgresource.extended.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.service.DemoProvenance;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component @Path("/v2/demo/metadata") @Produces(MediaType.APPLICATION_JSON)
public class DemoMetadataResource {
    @Autowired DemoProvenance provenance;

    @GET
    public Response get(@Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, provenance.publicMetadata(), request, true);
    }
}
