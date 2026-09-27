package itmo.is.lab1.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CopyTicketRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("100.0")
        Double discount
) {}
