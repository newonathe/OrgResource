package edu.orgresource.extended.api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/** Normalizes Jersey's provider-generated plain-text 400 responses to the API error contract. */
@Provider
public class C1ClientErrorResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (response.getStatus() != 400 || response.getMediaType() == null
                || MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getMediaType())) return;
        String path = request.getUriInfo().getPath();
        String code = path.endsWith("/receive") || path.endsWith("/adjust")
                || path.matches(".*/allocations/[^/]+/(approve|issue|return|consume|cancel)$")
                ? "INVALID_REQUEST_BODY" : "INVALID_REQUEST";
        response.setEntity(new ApiDtos.ErrorView(code, "The request could not be parsed or contains an invalid parameter."));
        response.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
    }
}
