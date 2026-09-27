package itmo.is.lab1.dto;

import itmo.is.lab1.model.TicketType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TicketRequest(
        @NotBlank String name,
        @Valid @NotNull CoordinatesRequest coordinates,
        @Positive Long personId,
        @Positive Long eventId,
        @Positive Double price,
        TicketType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("100.0") Double discount,
        @NotNull @Positive Integer number,
        @NotNull @Positive Long venueId
) {}
