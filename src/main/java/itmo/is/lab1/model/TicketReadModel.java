package itmo.is.lab1.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TicketReadModel(
        int id,
        String name,
        CoordinatesDetails coordinates,
        LocalDate creationDate,
        PersonDetails person,
        EventDetails event,
        Double price,
        TicketType type,
        double discount,
        Integer number,
        VenueDetails venue
) {

    public record CoordinatesDetails(int x, Long y) {}

    public record LocationDetails(double x, Integer y, long z, String name) {}

    public record PersonDetails(
            Long id,
            Color eyeColor,
            Color hairColor,
            LocationDetails location,
            Integer height,
            Country nationality
    ) {}

    public record EventDetails(
            Long id,
            String name,
            LocalDateTime date,
            long minAge,
            int ticketsCount,
            String description
    ) {}

    public record VenueDetails(
            long id,
            String name,
            int capacity,
            VenueType type
    ) {}
}
