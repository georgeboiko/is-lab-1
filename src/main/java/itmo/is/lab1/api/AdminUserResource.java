package itmo.is.lab1.api;

import itmo.is.lab1.api.security.UserPrincipal;
import itmo.is.lab1.dto.request.UpdateUserRoleRequest;
import itmo.is.lab1.dto.response.UserResponse;
import itmo.is.lab1.model.UserAccount;
import itmo.is.lab1.service.AdminUserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.java.Log;

import java.util.List;

@Log
@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
public class AdminUserResource {

    @Inject
    private AdminUserService adminUserService;

    @GET
    public List<UserResponse> findAll(@Context SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return adminUserService.findAll(principal.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    @PUT
    @Path("/{id}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    public UserResponse updateRole(@Context SecurityContext securityContext,
                                   @PathParam("id") long id,
                                   @Valid @NotNull UpdateUserRoleRequest input) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        UserAccount user = adminUserService.updateRole(principal.id(), id, input);
        log.info(() -> "User role set: actorId=" + principal.id()
                + ", userId=" + user.getId() + ", role=" + user.getRole());
        return toResponse(user);
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
