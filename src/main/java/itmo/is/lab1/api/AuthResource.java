package itmo.is.lab1.api;

import itmo.is.lab1.api.security.BearerTokens;
import itmo.is.lab1.api.security.UserPrincipal;
import itmo.is.lab1.dto.request.CredentialsRequest;
import itmo.is.lab1.dto.response.AuthResponse;
import itmo.is.lab1.dto.response.UserResponse;
import itmo.is.lab1.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid @NotNull CredentialsRequest input) {
        AuthService.LoginResult result = authService.register(input);
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(result))
                .build();
    }

    @POST
    @Path("/login")
    public AuthResponse login(@Valid @NotNull CredentialsRequest input) {
        return toResponse(authService.login(input));
    }

    @POST
    @Path("/logout")
    public Response logout(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(BearerTokens.fromHeader(authorization));
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    public UserResponse me(@Context SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return new UserResponse(principal.id(), principal.name());
    }

    private AuthResponse toResponse(AuthService.LoginResult result) {
        return new AuthResponse(
                result.token(), result.expiresAt(),
                result.userId(), result.username());
    }
}
