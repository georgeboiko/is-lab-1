package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.json.bind.annotation.JsonbTransient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
public class Location {

    @Id
    @Column(name = "person_id")
    @Setter(AccessLevel.NONE)
    private Long personId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @Setter(AccessLevel.PACKAGE)
    @JsonbTransient
    private Person person;

    @Column(nullable = false)
    private double x;

    @NotNull
    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private long z;

    @NotNull
    @Column(nullable = false)
    private String name;

    protected Location() {
    }

    public Location(double x, Integer y, long z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }
}
