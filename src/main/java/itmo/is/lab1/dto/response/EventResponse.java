package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        LocalDateTime date,
        long minAge,
        int ticketsCount,
        String description
) {

    public static EventResponse from(Event event) {
        return event == null ? null : new EventResponse(
                event.getId(),
                event.getName(),
                event.getDate(),
                event.getMinAge(),
                event.getTicketsCount(),
                event.getDescription()
        );
    }
}
