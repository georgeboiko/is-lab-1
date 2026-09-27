package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Coordinates;

public record CoordinatesResponse(int x, Long y) {

    public static CoordinatesResponse from(Coordinates coordinates) {
        return new CoordinatesResponse(coordinates.getX(), coordinates.getY());
    }
}
