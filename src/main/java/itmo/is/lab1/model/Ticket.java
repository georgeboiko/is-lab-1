package itmo.is.lab1.model;

import jakarta.persistence.Column;
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

import java.time.LocalDate;

@Entity
@Table(name = "tickets")
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
