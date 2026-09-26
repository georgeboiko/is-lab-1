package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Coordinates {

    @Max(222)
    @Column(name = "coordinates_x", nullable = false)
    private int x;

    @NotNull
    @Column(name = "coordinates_y", nullable = false)
    private Long y;

    protected Coordinates() {
    }

    public Coordinates(int x, Long y) {
        this.x = x;
        this.y = y;
    }
}
