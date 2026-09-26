package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(name = "event_date", nullable = false)
    private LocalDateTime date;

    @Column(name = "min_age", nullable = false)
    private long minAge;

    @Positive
    @Column(name = "tickets_count", nullable = false)
    private int ticketsCount;

    @NotBlank
    @Column(nullable = false)
    private String description;

    protected Event() {
    }

    public Event(String name, LocalDateTime date, long minAge, int ticketsCount, String description) {
        this.name = name;
        this.date = date;
        this.minAge = minAge;
        this.ticketsCount = ticketsCount;
        this.description = description;
    }
}
