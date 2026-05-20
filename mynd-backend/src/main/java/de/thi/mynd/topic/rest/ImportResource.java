package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.importer.FullImportDto;
import de.thi.mynd.topic.service.ImportService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/topics")
@Authenticated
public final class ImportResource {

    @Inject
    ImportService importService;

    @POST
    @Path("/import")
    @RolesAllowed("builder")
    @Operation(
            summary = "Imports multiple topics",
            description = "Imports multiple topics as JSON from a specific schema")
    @APIResponse(responseCode = "400", description = "Invalid import data")
    public Response importTopics(@Valid FullImportDto importDto) {
        importService.importFull(importDto, false);
        return Response.status(Response.Status.CREATED).build();
    }

}
