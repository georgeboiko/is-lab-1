package itmo.is.lab1.dto;

import itmo.is.lab1.model.Color;
import itmo.is.lab1.model.Country;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PersonRequest(
        Color eyeColor,
        @NotNull Color hairColor,
        @Valid LocationRequest location,
        @Positive Integer height,
        Country nationality
) {}
