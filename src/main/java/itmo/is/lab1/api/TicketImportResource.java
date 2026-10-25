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
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

@Log
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
        log.info(() -> "Import started: operationId=" + operationId + ", userId=" + principal.id());

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
            log.fine(() -> "Import file parsed: operationId=" + operationId + ", tickets=" + input.tickets().size());
            result = importService.importTickets(operationId, input);
        } catch (RuntimeException exception) {
            String message = exception instanceof TicketImportException
                    ? exception.getMessage() : "Import failed due to a server error";
            try {
                historyService.fail(operationId, message);
            } catch (RuntimeException historyException) {
                log.log(Level.SEVERE, "Could not record failed import: operationId=" + operationId, historyException);
                exception.addSuppressed(historyException);
            }
            if (exception instanceof TicketImportException importException) {
                log.warning(() -> "Import rejected: operationId=" + operationId
                        + ", recordNumber=" + importException.getRecordNumber());
                throw importException.withOperationId(operationId);
            }
            log.warning(() -> "Import failed: operationId=" + operationId);
            throw exception;
        }

        log.info(() -> "Import completed: operationId=" + operationId + ", tickets=" + result.ticketIds().size());
        result.createdVenueIds().forEach(id -> updateNotifier.notify("venue", "created", id));
        result.createdEventIds().forEach(id -> updateNotifier.notify("event", "created", id));
        result.createdPersonIds().forEach(id -> updateNotifier.notify("person", "created", id));
        result.ticketIds().forEach(id -> updateNotifier.notify("ticket", "created", id));
        return new TicketImportResponse(operationId, result.ticketIds().size());
    }
}
