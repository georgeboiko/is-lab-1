package itmo.is.lab1.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(
        @NotNull @Pattern(regexp = "USER|ADMIN", message = "Role must be USER or ADMIN")
        String role
) {}
