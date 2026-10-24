package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.EventRequest;
import itmo.is.lab1.model.Event;
import itmo.is.lab1.repository.EventRepository;
import itmo.is.lab1.repository.TicketRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class EventService {

    @Inject
    private EventRepository eventRepository;

    @Inject
    private TicketRepository ticketRepository;

    @Transactional
    public Event create(@Valid @NotNull EventRequest input) {
        Event event = new Event(
                input.name(), input.date(), input.minAge(),
                input.ticketsCount(), input.description());
        eventRepository.persist(event);
        return event;
    }

    public Event findById(long id) {
        return requireEvent(id);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public List<Event> findByNameAndDate(String name, LocalDateTime date) {
        return eventRepository.findByNameAndDate(name, date);
    }

    @Transactional
    public Event update(long id, @Valid @NotNull EventRequest input) {
        Event event = requireEvent(id);
        event.setName(input.name());
        event.setDate(input.date());
        event.setMinAge(input.minAge());
        event.setTicketsCount(input.ticketsCount());
        event.setDescription(input.description());
        return event;
    }

    @Transactional
    public void delete(long id) {
        Event event = requireEvent(id);
        if (ticketRepository.existsByEventId(id)) {
            throw new IllegalStateException(
                    "Event with id " + id + " is used by a ticket and cannot be deleted");
        }
        eventRepository.remove(event);
    }

    private Event requireEvent(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event with id " + id + " was not found"));
    }
}
