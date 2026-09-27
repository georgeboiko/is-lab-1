package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Venue;
import itmo.is.lab1.model.VenueType;

public record VenueResponse(long id, String name, int capacity, VenueType type) {

    public static VenueResponse from(Venue venue) {
        return venue == null ? null : new VenueResponse(
                venue.getId(), venue.getName(), venue.getCapacity(), venue.getType());
    }
}
