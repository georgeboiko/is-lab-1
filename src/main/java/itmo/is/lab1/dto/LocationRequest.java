package itmo.is.lab1.dto;

import jakarta.validation.constraints.NotNull;

public record LocationRequest(
        @NotNull Double x,
        @NotNull Integer y,
        @NotNull Long z,
        @NotNull String name
) {}
