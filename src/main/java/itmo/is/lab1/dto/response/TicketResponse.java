package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Ticket;
import itmo.is.lab1.model.TicketType;

import java.time.LocalDate;

public record TicketResponse(
        int id,
        String name,
        CoordinatesResponse coordinates,
        LocalDate creationDate,
        PersonResponse person,
        EventResponse event,
        Double price,
        TicketType type,
        double discount,
        Integer number,
        VenueResponse venue
) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getName(),
                CoordinatesResponse.from(ticket.getCoordinates()),
                ticket.getCreationDate(),
                PersonResponse.from(ticket.getPerson()),
                EventResponse.from(ticket.getEvent()),
                ticket.getPrice(),
                ticket.getType(),
                ticket.getDiscount(),
                ticket.getNumber(),
                VenueResponse.from(ticket.getVenue())
        );
    }
}
