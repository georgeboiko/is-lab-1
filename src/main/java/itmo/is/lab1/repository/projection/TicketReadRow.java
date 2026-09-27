package itmo.is.lab1.repository.projection;

import itmo.is.lab1.model.Color;
import itmo.is.lab1.model.Country;
import itmo.is.lab1.model.TicketReadModel;
import itmo.is.lab1.model.TicketType;
import itmo.is.lab1.model.VenueType;

import java.sql.Date;
import java.sql.Timestamp;

public record TicketReadRow(
        Integer ticketId,
        String ticketName,
        Integer coordinatesX,
        Long coordinatesY,
        Date creationDate,
        Double price,
        String ticketType,
        Double discount,
        Integer number,
        Long personId,
        String eyeColor,
        String hairColor,
        Integer height,
        String nationality,
        Double locationX,
        Integer locationY,
        Long locationZ,
        String locationName,
        Long eventId,
        String eventName,
        Timestamp eventDate,
        Long minAge,
        Integer ticketsCount,
        String eventDescription,
        Long venueId,
        String venueName,
        Integer venueCapacity,
        String venueType
) {

    public TicketReadModel toReadModel() {
        TicketReadModel.PersonDetails person = personId == null
                ? null
                : new TicketReadModel.PersonDetails(
                        personId,
                        enumValue(Color.class, eyeColor),
                        enumValue(Color.class, hairColor),
                        location(),
                        height,
                        enumValue(Country.class, nationality)
                );

        TicketReadModel.EventDetails event = eventId == null
                ? null
                : new TicketReadModel.EventDetails(
                        eventId,
                        eventName,
                        eventDate.toLocalDateTime(),
                        minAge,
                        ticketsCount,
                        eventDescription
                );

        return new TicketReadModel(
                ticketId,
                ticketName,
                new TicketReadModel.CoordinatesDetails(coordinatesX, coordinatesY),
                creationDate.toLocalDate(),
                person,
                event,
                price,
                enumValue(TicketType.class, ticketType),
                discount,
                number,
                new TicketReadModel.VenueDetails(
                        venueId,
                        venueName,
                        venueCapacity,
                        enumValue(VenueType.class, venueType)
                )
        );
    }

    private TicketReadModel.LocationDetails location() {
        return locationX == null ? null : new TicketReadModel.LocationDetails(
                locationX, locationY, locationZ, locationName);
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
