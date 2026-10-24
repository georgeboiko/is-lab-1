package itmo.is.lab1.api;

import itmo.is.lab1.api.security.UserPrincipal;
import itmo.is.lab1.dto.response.ImportOperationResponse;
import itmo.is.lab1.dto.response.PageResponse;
import itmo.is.lab1.service.ImportHistoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/imports")
@Produces(MediaType.APPLICATION_JSON)
public class ImportHistoryResource {

    @Inject
    private ImportHistoryService historyService;

    @GET
    public PageResponse<ImportOperationResponse> findPage(
            @Context SecurityContext securityContext,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return PageResponse.from(historyService.findPage(principal.id(), page, size),
                ImportOperationResponse::from);
    }

    @GET
    @Path("/{id}")
    public ImportOperationResponse findById(@Context SecurityContext securityContext,
                                           @PathParam("id") long id) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return ImportOperationResponse.from(historyService.findById(principal.id(), id));
    }
}
