package itmo.is.lab1.api.error;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER - 100)
public class TransactionalExceptionMapper implements ExceptionMapper<TransactionalException> {

    @Inject
    private ServerExceptionMapper serverExceptionMapper;

    @Override
    public Response toResponse(TransactionalException exception) {
        return serverExceptionMapper.toResponse(exception);
    }
}
