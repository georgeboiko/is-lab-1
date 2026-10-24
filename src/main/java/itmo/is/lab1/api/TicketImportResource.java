package itmo.is.lab1.api;

import itmo.is.lab1.api.security.UserPrincipal;
import itmo.is.lab1.dto.request.TicketImportRequest;
import itmo.is.lab1.dto.response.TicketImportResponse;
import itmo.is.lab1.service.ImportHistoryService;
import itmo.is.lab1.service.TicketImportParser;
import itmo.is.lab1.service.TicketImportService;
import itmo.is.lab1.service.TicketImportService.ImportResult;
import itmo.is.lab1.service.error.TicketImportException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.io.InputStream;

@Path("/tickets/import")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class TicketImportResource {

    @Inject
    private TicketImportParser parser;

    @Inject
    private TicketImportService importService;

    @Inject
    private ImportHistoryService historyService;

    @Inject
    private UpdateNotifier updateNotifier;

    @POST
    public TicketImportResponse importTickets(@FormParam("file") EntityPart file,
                                              @Context SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        long operationId = historyService.start(principal.id());

        ImportResult result;
        try {
            if (file == null) {
                throw TicketImportException.invalid("file", "A JSON file is required");
            }

            TicketImportRequest input;
            try (InputStream content = file.getContent()) {
                input = parser.parse(content);
            } catch (IOException exception) {
                throw TicketImportException.invalid("file", "Could not read the uploaded file");
            }
            result = importService.importTickets(operationId, input);
        } catch (RuntimeException exception) {
            String message = exception instanceof TicketImportException
                    ? exception.getMessage() : "Import failed due to a server error";
            historyService.fail(operationId, message);
            if (exception instanceof TicketImportException importException) {
                throw importException.withOperationId(operationId);
            }
            throw exception;
        }

        result.createdVenueIds().forEach(id -> updateNotifier.notify("venue", "created", id));
        result.createdEventIds().forEach(id -> updateNotifier.notify("event", "created", id));
        result.createdPersonIds().forEach(id -> updateNotifier.notify("person", "created", id));
        result.ticketIds().forEach(id -> updateNotifier.notify("ticket", "created", id));
        return new TicketImportResponse(operationId, result.ticketIds().size());
    }
}
