package itmo.is.lab1.api.security;

import java.security.Principal;

public record UserPrincipal(Long id, String name) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
