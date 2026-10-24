package itmo.is.lab1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TicketImportRequest(
        @NotEmpty @Size(max = TicketImportRequest.MAX_TICKETS)
        List<@NotNull @Valid TicketImportItem> tickets
) {
    public static final int MAX_TICKETS = 1000;
}
