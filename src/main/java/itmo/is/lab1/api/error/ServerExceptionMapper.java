package itmo.is.lab1.api.error;

import itmo.is.lab1.dto.response.ApiError;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.java.Log;

import javax.transaction.xa.XAException;
import java.sql.SQLException;
import java.util.logging.Level;

@Log
@Provider
@ApplicationScoped
public class ServerExceptionMapper implements ExceptionMapper<Exception> {

    private static final String SERIALIZATION_FAILURE_SQL_STATE = "40001";
    private static final String GLASSFISH_LOCAL_TRANSACTION_PREFIX =
            "jakarta.resource.spi.LocalTransactionException: ERROR: ";

    @Override
    public Response toResponse(Exception exception) {
        if (isSerializationFailure(exception)) {
            log.warning("Transaction rolled back due to a serialization conflict");
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiError("Concurrent modification detected; please retry the request"))
                    .build();
        }

        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof WebApplicationException webException) {
                if (webException.getResponse().getStatus() >= 500) {
                    log.log(Level.SEVERE, "Request failed", exception);
                }
                return webException.getResponse();
            }
        }

        log.log(Level.SEVERE, "Unhandled server error", exception);
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
            if (isGlassFishCommitSerializationFailure(cause)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGlassFishCommitSerializationFailure(Throwable exception) {
        if (!(exception instanceof TransactionalException)
                || !(exception.getCause() instanceof SystemException systemException)
                || !(systemException.getCause() instanceof XAException xaException)
                || xaException.getCause() != null) {
            return false;
        }

        String message = xaException.getMessage();
        if (message == null || !message.startsWith(GLASSFISH_LOCAL_TRANSACTION_PREFIX)) {
            return false;
        }
        String detail = message.substring(GLASSFISH_LOCAL_TRANSACTION_PREFIX.length());
        int lineEnd = detail.indexOf('\n');
        if (lineEnd >= 0) {
            detail = detail.substring(0, lineEnd);
        }
        return switch (detail.stripTrailing()) {
            case "could not serialize access due to read/write dependencies among transactions",
                 "could not serialize access due to concurrent update",
                 "could not serialize access due to concurrent delete" -> true;
            default -> false;
        };
    }
}
