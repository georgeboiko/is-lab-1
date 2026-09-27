package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.TicketReadModel;
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

    public static TicketResponse from(TicketReadModel ticket) {
        TicketReadModel.PersonDetails person = ticket.person();
        TicketReadModel.EventDetails event = ticket.event();
        TicketReadModel.VenueDetails venue = ticket.venue();

        return new TicketResponse(
                ticket.id(),
                ticket.name(),
                new CoordinatesResponse(
                        ticket.coordinates().x(), ticket.coordinates().y()),
                ticket.creationDate(),
                person == null ? null : new PersonResponse(
                        person.id(),
                        person.eyeColor(),
                        person.hairColor(),
                        locationFrom(person.location()),
                        person.height(),
                        person.nationality()
                ),
                event == null ? null : new EventResponse(
                        event.id(),
                        event.name(),
                        event.date(),
                        event.minAge(),
                        event.ticketsCount(),
                        event.description()
                ),
                ticket.price(),
                ticket.type(),
                ticket.discount(),
                ticket.number(),
                new VenueResponse(
                        venue.id(), venue.name(), venue.capacity(), venue.type())
        );
    }

    private static LocationResponse locationFrom(
            TicketReadModel.LocationDetails location) {
        return location == null ? null : new LocationResponse(
                location.x(), location.y(), location.z(), location.name());
    }
}
