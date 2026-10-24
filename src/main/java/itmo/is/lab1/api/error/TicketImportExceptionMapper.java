package itmo.is.lab1.api.error;

import itmo.is.lab1.service.error.TicketImportException;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TicketImportExceptionMapper
        implements ExceptionMapper<TicketImportException> {

    @Override
    public Response toResponse(TicketImportException exception) {
        JsonObjectBuilder error = Json.createObjectBuilder()
                .add("message", exception.getMessage())
                .add("field", exception.getField());
        if (exception.getRecordNumber() != null) {
            error.add("recordNumber", exception.getRecordNumber());
        }
        if (exception.getOperationId() != null) {
            error.add("operationId", exception.getOperationId());
        }

        return Response.status(exception.isConflict()
                        ? Response.Status.CONFLICT : Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error.build())
                .build();
    }
}
