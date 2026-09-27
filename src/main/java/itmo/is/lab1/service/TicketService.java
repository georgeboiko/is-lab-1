package itmo.is.lab1.service;

import itmo.is.lab1.dto.CoordinatesRequest;
import itmo.is.lab1.dto.PageResult;
import itmo.is.lab1.dto.TicketRequest;
import itmo.is.lab1.model.Coordinates;
import itmo.is.lab1.model.Event;
import itmo.is.lab1.model.Person;
import itmo.is.lab1.model.Ticket;
import itmo.is.lab1.model.Venue;
import itmo.is.lab1.repository.EventRepository;
import itmo.is.lab1.repository.PersonRepository;
import itmo.is.lab1.repository.TicketRepository;
import itmo.is.lab1.repository.VenueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TicketService {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private PersonRepository personRepository;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private VenueRepository venueRepository;

    @Transactional
    public Ticket create(@Valid @NotNull TicketRequest input) {
        Venue venue = requireVenue(input.venueId());
        Ticket ticket = new Ticket(
                input.name(),
                copyCoordinates(input.coordinates()),
                input.discount(),
                input.number(),
                venue
        );
        applyInput(ticket, input, venue);
        ticketRepository.persist(ticket);
        return ticket;
    }

    public Ticket findById(int id) {
        return requireTicket(id);
    }

    public PageResult<Ticket> findPage(int page, int size, String nameFilter,
                                       boolean ascending) {
        validatePage(page, size);
        int offset = Math.multiplyExact(page, size);
        return new PageResult<>(
                ticketRepository.findPage(offset, size, nameFilter, ascending),
                ticketRepository.count(nameFilter),
                page,
                size
        );
    }

    @Transactional
    public Ticket update(int id, @Valid @NotNull TicketRequest input) {
        Ticket ticket = requireTicket(id);
        Venue venue = requireVenue(input.venueId());
        applyInput(ticket, input, venue);
        return ticket;
    }

    @Transactional
    public void delete(int id) {
        ticketRepository.remove(requireTicket(id));
    }

    public Optional<Ticket> findWithMaxId() {
        return ticketRepository.findWithMaxId();
    }

    public List<Ticket> findByNamePrefix(String prefix) {
        return ticketRepository.findByNamePrefix(prefix);
    }

    public List<Ticket> findByVenueCapacityGreaterThan(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Venue capacity must not be negative");
        }
        return ticketRepository.findByVenueCapacityGreaterThan(capacity);
    }

    @Transactional
    public Ticket sell(int ticketId, double amount, long personId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Sale amount must be greater than zero");
        }
        requirePerson(personId);
        return ticketRepository.sell(ticketId, amount, personId)
                .orElseThrow(() -> ticketNotFound(ticketId));
    }

    @Transactional
    public Ticket copyWithDiscount(int ticketId, double discount) {
        if (discount <= 0 || discount > 100) {
            throw new IllegalArgumentException(
                    "Discount must be greater than zero and at most 100");
        }

        Ticket source = requireTicket(ticketId);
        if (source.getPrice() == null) {
            throw new IllegalStateException(
                    "Ticket with id " + ticketId + " has no price");
        }

        return ticketRepository.copyWithDiscount(ticketId, discount)
                .orElseThrow(() -> new IllegalStateException(
                        "Ticket copy was not created"));
    }

    private void applyInput(Ticket ticket, TicketRequest input, Venue venue) {
        ticket.setName(input.name());
        ticket.setCoordinates(copyCoordinates(input.coordinates()));
        ticket.setPerson(input.personId() == null ? null : requirePerson(input.personId()));
        ticket.setEvent(input.eventId() == null ? null : requireEvent(input.eventId()));
        ticket.setPrice(input.price());
        ticket.setType(input.type());
        ticket.setDiscount(input.discount());
        ticket.setNumber(input.number());
        ticket.setVenue(venue);
    }

    private Coordinates copyCoordinates(CoordinatesRequest coordinates) {
        return new Coordinates(coordinates.x(), coordinates.y());
    }

    private Ticket requireTicket(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> ticketNotFound(id));
    }

    private Person requirePerson(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Person with id " + id + " was not found"));
    }

    private Event requireEvent(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event with id " + id + " was not found"));
    }

    private Venue requireVenue(long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Venue with id " + id + " was not found"));
    }

    private EntityNotFoundException ticketNotFound(int id) {
        return new EntityNotFoundException(
                "Ticket with id " + id + " was not found");
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}
