package app.rest.controllers;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.components.ResourceComponent;
import app.components.ResourceImageSupport;
import app.components.RequestValidation;
import app.dto.ApiDtos.CreateResource;
import app.dto.ApiDtos.ResourceAvailabilityView;
import app.dto.ApiDtos.ResourceImageUploadView;
import app.dto.ApiDtos.ResourceView;
import app.dto.ApiDtos.UpdateResource;
import app.entities.ResourceType;

@Component
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourceController {
    @Autowired private ResourceComponent component;
    @Autowired private RequestValidation requestValidation;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateResource request) {
        requestValidation.validate(request);
        return Response.status(Response.Status.CREATED).entity(component.create(request.name,
                request.type, request.totalQuantity)).build();
    }

    @GET
    public List<ResourceView> list(@QueryParam("type") ResourceType type) {
        return component.list(type);
    }

    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, UpdateResource request) {
        requestValidation.validate(request);
        return Response.ok(component.update(id, request.name, request.totalQuantity,
                request.expectedTotalQuantity)).build();
    }

    @POST
    @Path("/{id}/delete")
    public Response delete(@PathParam("id") Long id) {
        return Response.ok(component.delete(id)).build();
    }

    @GET
    @Path("/{id}/availability")
    public ResourceAvailabilityView availability(@PathParam("id") Long id) {
        return component.getAvailability(id);
    }

    @POST
    @Path("/{id}/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(@PathParam("id") Long id,
            @FormDataParam("file") FormDataBodyPart filePart) {
        if (filePart == null || filePart.getMediaType() == null) {
            throw new app.components.ApiException(400, "INVALID_IMAGE",
                    "A file part with image/png or image/jpeg content type is required.");
        }
        byte[] bytes;
        try (InputStream input = filePart.getValueAs(InputStream.class)) {
            if (input == null) {
                throw new app.components.ApiException(400, "INVALID_IMAGE", "The file part is empty.");
            }
            bytes = ResourceImageSupport.readBounded(input);
        } catch (IOException invalidUpload) {
            throw new app.components.ApiException(400, "INVALID_IMAGE", "The image upload could not be read.");
        }
        ResourceImageUploadView result = component.storeImage(id, bytes, filePart.getMediaType().toString());
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}/image")
    @Produces({"image/png", "image/jpeg"})
    public Response getImage(@PathParam("id") Long id) {
        ResourceComponent.ResourceImageContent image = component.getImage(id);
        return Response.ok(image.bytes, image.contentType)
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }
}
