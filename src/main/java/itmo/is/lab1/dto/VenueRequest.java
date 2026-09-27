package itmo.is.lab1.dto;

import itmo.is.lab1.model.VenueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VenueRequest(
        @NotBlank String name,
        @NotNull @Positive Integer capacity,
        @NotNull VenueType type
) {}
