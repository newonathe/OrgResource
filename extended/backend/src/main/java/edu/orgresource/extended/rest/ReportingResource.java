package edu.orgresource.extended.rest;

import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.service.ReportingComponent;
import edu.orgresource.extended.service.ReportingComponent.ExportFile;
import edu.orgresource.extended.service.ReportingComponent.Format;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v2/orgs/{organizationId}")
public class ReportingResource {
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'", Locale.ROOT)
            .withZone(ZoneOffset.UTC);
    @Autowired ReportingComponent reporting;

    @GET @Path("/audit") @Produces(MediaType.APPLICATION_JSON)
    public Response audit(@PathParam("organizationId") UUID orgId, @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("cursor") String cursor, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, reporting.events(orgId, limit, cursor, true), request, true);
    }

    @GET @Path("/activity") @Produces(MediaType.APPLICATION_JSON)
    public Response activity(@PathParam("organizationId") UUID orgId, @DefaultValue("50") @QueryParam("limit") int limit,
            @QueryParam("cursor") String cursor, @Context HttpServletRequest request) {
        return C1HttpResponses.envelope(200, reporting.events(orgId, limit, cursor, false), request, true);
    }

    @POST @Path("/exports") @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ "text/csv", MediaType.APPLICATION_JSON })
    public Response export(@PathParam("organizationId") UUID orgId, InputStream body, @Context HttpServletRequest request) {
        ExportFile file = reporting.export(orgId, body);
        boolean csv = file.format() == Format.CSV;
        String name = "orgresource-" + file.dataset().name().toLowerCase(Locale.ROOT) + "-"
                + FILE_DATE.format(file.exportedAt()) + (csv ? ".csv" : ".json");
        return Response.ok(file.bytes(), csv ? "text/csv; charset=UTF-8" : MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=\"" + name + "\"")
                .header("Cache-Control", "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Request-ID", C1HttpResponses.requestId(request))
                .header("X-Export-Schema-Version", ReportingComponent.SCHEMA_VERSION)
                .header("X-Exported-At", file.exportedAt().toString())
                .build();
    }
}
