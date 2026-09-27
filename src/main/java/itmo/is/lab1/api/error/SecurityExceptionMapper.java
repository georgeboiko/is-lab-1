package itmo.is.lab1.api.error;

import itmo.is.lab1.dto.response.ApiError;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

    @Override
    public Response toResponse(SecurityException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(exception.getMessage()))
                .build();
    }
}
