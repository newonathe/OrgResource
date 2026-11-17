package edu.orgresource.extended.rest;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.api.C1RequestValidator;
import edu.orgresource.extended.service.C1Component;
import edu.orgresource.extended.domain.ResourceType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import edu.orgresource.extended.service.ResourceImageSupport;

@Component @Path("/resources") @Produces(MediaType.APPLICATION_JSON)
public class ResourceResource {
    @Autowired private C1Component component;
    @Autowired private C1RequestValidator requestValidator;
    @POST @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ApiDtos.CreateResource request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.createResource(request)).build();
    }
    @POST @Path("/{id}/update") @Consumes(MediaType.APPLICATION_JSON)
    public ApiDtos.ResourceView update(@PathParam("id") Long id, ApiDtos.ResourceUpdate request) {
        requestValidator.validate(request);
        return component.updateResource(id, request);
    }
    @POST @Path("/{id}/delete")
    public ApiDtos.MessageView delete(@PathParam("id") Long id) { return component.deleteResource(id); }
    @GET public List<ApiDtos.ResourceView> list(@QueryParam("type") ResourceType type) { return component.listResources(type); }

    @GET @Path("/{id}/availability")
    public ApiDtos.ResourceAvailabilityView availability(@PathParam("id") Long id) {
        return component.resourceAvailability(id);
    }

    @POST @Path("/{id}/image") @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(@PathParam("id") Long id, @FormDataParam("file") FormDataBodyPart filePart) {
        if (filePart == null || filePart.getMediaType() == null)
            throw new edu.orgresource.extended.api.C1ApiException(400, "INVALID_IMAGE", "A file part with image/png or image/jpeg content type is required.");
        try (InputStream input = filePart.getValueAs(InputStream.class)) {
            if (input == null) throw new edu.orgresource.extended.api.C1ApiException(400, "INVALID_IMAGE", "The file part is empty.");
            byte[] bytes = ResourceImageSupport.readBounded(input);
            return Response.ok(component.storeResourceImage(id, bytes, filePart.getMediaType().toString())).build();
        } catch (IOException invalidUpload) {
            throw new edu.orgresource.extended.api.C1ApiException(400, "INVALID_IMAGE", "The image upload could not be read.");
        }
    }

    @GET @Path("/{id}/image") @Produces({"image/png", "image/jpeg"})
    public Response getImage(@PathParam("id") Long id) {
        edu.orgresource.extended.service.C1Service.ResourceImageContent stored = component.resourceImage(id);
        return Response.ok(stored.bytes(), stored.contentType())
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

}
