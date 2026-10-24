package itmo.is.lab1.api.security;

import itmo.is.lab1.model.UserRole;

import java.security.Principal;

public record UserPrincipal(Long id, String name, UserRole role) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
