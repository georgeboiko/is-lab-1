package itmo.is.lab1.dto.request;

import itmo.is.lab1.model.TicketType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TicketImportItem(
        @NotBlank String name,
        @Valid @NotNull CoordinatesRequest coordinates,
        @Valid PersonRequest person,
        @Valid EventRequest event,
        @Positive Double price,
        TicketType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("100.0") Double discount,
        @NotNull @Positive Integer number,
        @Valid @NotNull VenueRequest venue
) {}
