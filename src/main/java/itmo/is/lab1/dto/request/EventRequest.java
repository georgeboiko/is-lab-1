package itmo.is.lab1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank String name,
        @NotNull LocalDateTime date,
        @NotNull Long minAge,
        @NotNull @Positive Integer ticketsCount,
        @NotBlank String description
) {}
