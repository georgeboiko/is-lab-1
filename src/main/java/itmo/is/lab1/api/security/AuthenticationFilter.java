package itmo.is.lab1.api.security;

import itmo.is.lab1.dto.response.ApiError;
import itmo.is.lab1.service.AuthService;
import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private AuthService authService;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (HttpMethod.OPTIONS.equals(requestContext.getMethod()) || isPermitAll()) {
            return;
        }

        String rawToken = BearerTokens.fromHeader(
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
        AuthService.AuthenticatedUser user = authService.authenticate(rawToken)
                .orElse(null);
        if (user == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiError("A valid Bearer token is required"))
                    .build());
            return;
        }

        SecurityContext original = requestContext.getSecurityContext();
        UserPrincipal principal = new UserPrincipal(user.id(), user.username());
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public UserPrincipal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return original != null && original.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }

    private boolean isPermitAll() {
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        return method != null && method.isAnnotationPresent(PermitAll.class)
                || resourceClass != null
                && resourceClass.isAnnotationPresent(PermitAll.class);
    }
}
