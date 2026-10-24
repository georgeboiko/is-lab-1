package itmo.is.lab1.api.error;

import itmo.is.lab1.dto.response.ApiError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.sql.SQLException;

@Provider
public class ServerExceptionMapper implements ExceptionMapper<Exception> {

    private static final String SERIALIZATION_FAILURE_SQL_STATE = "40001";

    @Override
    public Response toResponse(Exception exception) {
        if (isSerializationFailure(exception)) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiError("Concurrent modification detected; please retry the request"))
                    .build();
        }

        if (exception instanceof WebApplicationException webException) {
            return webException.getResponse();
        }

        return Response.serverError()
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("Internal server error"))
                .build();
    }

    private boolean isSerializationFailure(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                for (SQLException sql = sqlException; sql != null; sql = sql.getNextException()) {
                    if (SERIALIZATION_FAILURE_SQL_STATE.equals(sql.getSQLState())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
