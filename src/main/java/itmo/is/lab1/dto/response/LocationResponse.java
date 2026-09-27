package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Location;

public record LocationResponse(double x, Integer y, long z, String name) {

    public static LocationResponse from(Location location) {
        return location == null ? null : new LocationResponse(
                location.getX(), location.getY(), location.getZ(), location.getName());
    }
}
