package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import itmo.is.lab1.repository.projection.TicketReadRow;

import java.time.LocalDate;

@Entity
@Table(name = "tickets")
@SqlResultSetMapping(
        name = "TicketReadRowMapping",
        classes = @ConstructorResult(
                targetClass = TicketReadRow.class,
                columns = {
                        @ColumnResult(name = "ticket_id", type = Integer.class),
                        @ColumnResult(name = "ticket_name", type = String.class),
                        @ColumnResult(name = "coordinates_x", type = Integer.class),
                        @ColumnResult(name = "coordinates_y", type = Long.class),
                        @ColumnResult(name = "creation_date", type = java.sql.Date.class),
                        @ColumnResult(name = "price", type = Double.class),
                        @ColumnResult(name = "ticket_type", type = String.class),
                        @ColumnResult(name = "discount", type = Double.class),
                        @ColumnResult(name = "number", type = Integer.class),
                        @ColumnResult(name = "person_id", type = Long.class),
                        @ColumnResult(name = "eye_color", type = String.class),
                        @ColumnResult(name = "hair_color", type = String.class),
                        @ColumnResult(name = "height", type = Integer.class),
                        @ColumnResult(name = "nationality", type = String.class),
                        @ColumnResult(name = "location_x", type = Double.class),
                        @ColumnResult(name = "location_y", type = Integer.class),
                        @ColumnResult(name = "location_z", type = Long.class),
                        @ColumnResult(name = "location_name", type = String.class),
                        @ColumnResult(name = "event_id", type = Long.class),
                        @ColumnResult(name = "event_name", type = String.class),
                        @ColumnResult(name = "event_date", type = java.sql.Timestamp.class),
                        @ColumnResult(name = "min_age", type = Long.class),
                        @ColumnResult(name = "tickets_count", type = Integer.class),
                        @ColumnResult(name = "event_description", type = String.class),
                        @ColumnResult(name = "venue_id", type = Long.class),
                        @ColumnResult(name = "venue_name", type = String.class),
                        @ColumnResult(name = "venue_capacity", type = Integer.class),
                        @ColumnResult(name = "venue_type", type = String.class)
                }
        )
)
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Valid
    @NotNull
    @Embedded
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false,
            insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDate creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Positive
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private TicketType type;

    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("100.0")
    @Column(nullable = false)
    private double discount;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer number;

    @Valid
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    protected Ticket() {
    }

    public Ticket(String name, Coordinates coordinates, double discount,
                  Integer number, Venue venue) {
        this.name = name;
        this.coordinates = coordinates;
        this.discount = discount;
        this.number = number;
        this.venue = venue;
    }

}
