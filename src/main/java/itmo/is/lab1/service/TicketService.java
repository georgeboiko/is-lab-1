package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.CoordinatesRequest;
import itmo.is.lab1.dto.request.TicketRequest;
import itmo.is.lab1.model.Coordinates;
import itmo.is.lab1.model.Event;
import itmo.is.lab1.model.Page;
import itmo.is.lab1.model.Person;
import itmo.is.lab1.model.Ticket;
import itmo.is.lab1.model.TicketReadModel;
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
    public TicketReadModel create(@Valid @NotNull TicketRequest input) {
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
        return toReadModel(ticket);
    }

    public TicketReadModel findById(int id) {
        return requireTicketReadModel(id);
    }

    public Page<TicketReadModel> findPage(int page, int size,
                                          String filterField, String filterValue,
                                          String sortField, boolean ascending) {
        validatePage(page, size);
        int offset = Math.multiplyExact(page, size);
        return new Page<>(
                ticketRepository.findReadModelPage(
                        offset, size, filterField, filterValue,
                        sortField, ascending),
                ticketRepository.count(filterField, filterValue),
                page,
                size
        );
    }

    @Transactional
    public TicketReadModel update(int id, @Valid @NotNull TicketRequest input) {
        Ticket ticket = requireTicketEntity(id);
        Venue venue = requireVenue(input.venueId());
        applyInput(ticket, input, venue);
        return toReadModel(ticket);
    }

    @Transactional
    public void delete(int id) {
        ticketRepository.remove(requireTicketEntity(id));
    }

    public Optional<TicketReadModel> findWithMaxId() {
        return ticketRepository.findWithMaxId();
    }

    public List<TicketReadModel> findByNamePrefix(String prefix) {
        return ticketRepository.findByNamePrefix(prefix);
    }

    public List<TicketReadModel> findByVenueCapacityGreaterThan(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Venue capacity must not be negative");
        }
        return ticketRepository.findByVenueCapacityGreaterThan(capacity);
    }

    @Transactional
    public TicketReadModel sell(int ticketId, double amount, long personId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Sale amount must be greater than zero");
        }
        requirePerson(personId);
        return ticketRepository.sell(ticketId, amount, personId)
                .orElseThrow(() -> ticketNotFound(ticketId));
    }

    @Transactional
    public TicketReadModel copyWithDiscount(int ticketId, double discount) {
        if (discount <= 0 || discount > 100) {
            throw new IllegalArgumentException(
                    "Discount must be greater than zero and at most 100");
        }

        Ticket source = requireTicketEntity(ticketId);
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

    private TicketReadModel toReadModel(Ticket ticket) {
        Person person = ticket.getPerson();
        Event event = ticket.getEvent();
        Venue venue = ticket.getVenue();

        TicketReadModel.PersonDetails personDetails = person == null
                ? null
                : new TicketReadModel.PersonDetails(
                        person.getId(),
                        person.getEyeColor(),
                        person.getHairColor(),
                        person.getLocation() == null
                                ? null
                                : new TicketReadModel.LocationDetails(
                                        person.getLocation().getX(),
                                        person.getLocation().getY(),
                                        person.getLocation().getZ(),
                                        person.getLocation().getName()
                                ),
                        person.getHeight(),
                        person.getNationality()
                );

        TicketReadModel.EventDetails eventDetails = event == null
                ? null
                : new TicketReadModel.EventDetails(
                        event.getId(),
                        event.getName(),
                        event.getDate(),
                        event.getMinAge(),
                        event.getTicketsCount(),
                        event.getDescription()
                );

        return new TicketReadModel(
                ticket.getId(),
                ticket.getName(),
                new TicketReadModel.CoordinatesDetails(
                        ticket.getCoordinates().getX(),
                        ticket.getCoordinates().getY()),
                ticket.getCreationDate(),
                personDetails,
                eventDetails,
                ticket.getPrice(),
                ticket.getType(),
                ticket.getDiscount(),
                ticket.getNumber(),
                new TicketReadModel.VenueDetails(
                        venue.getId(), venue.getName(),
                        venue.getCapacity(), venue.getType())
        );
    }

    private Ticket requireTicketEntity(int id) {
        return ticketRepository.findEntityById(id)
                .orElseThrow(() -> ticketNotFound(id));
    }

    private TicketReadModel requireTicketReadModel(int id) {
        return ticketRepository.findReadModelById(id)
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
