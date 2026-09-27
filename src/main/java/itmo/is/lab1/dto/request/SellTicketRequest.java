package itmo.is.lab1.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SellTicketRequest(
        @NotNull @Positive Double amount,
        @NotNull @Positive Long personId
) {}
