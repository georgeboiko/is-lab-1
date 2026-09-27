package itmo.is.lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record CoordinatesRequest(
        @NotNull @Max(222) Integer x,
        @NotNull Long y
) {}
