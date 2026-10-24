package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.EventRequest;
import itmo.is.lab1.dto.request.PersonRequest;
import itmo.is.lab1.dto.request.TicketImportItem;
import itmo.is.lab1.dto.request.TicketImportRequest;
import itmo.is.lab1.dto.request.TicketRequest;
import itmo.is.lab1.dto.request.VenueRequest;
import itmo.is.lab1.model.Event;
import itmo.is.lab1.model.Person;
import itmo.is.lab1.model.TicketReadModel;
import itmo.is.lab1.model.Venue;
import itmo.is.lab1.service.error.TicketImportException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class TicketImportService {

    @Inject
    private TicketService ticketService;

    @Inject
    private VenueService venueService;

    @Inject
    private EventService eventService;

    @Inject
    private PersonService personService;

    @Transactional(rollbackOn = Exception.class)
    public ImportResult importTickets(@Valid @NotNull TicketImportRequest input) {
        ImportContext context = new ImportContext();
        for (int i = 0; i < input.tickets().size(); ++i) {
            TicketImportItem item = input.tickets().get(i);
            int recordNumber = i + 1;
            Venue venue = resolveVenue(item.venue(), recordNumber, context);
            Event event = resolveEvent(item.event(), recordNumber, context);
            Person person = resolvePerson(item.person(), recordNumber, context);

            TicketReadModel ticket = ticketService.create(new TicketRequest(
                    item.name(), item.coordinates(),
                    person == null ? null : person.getId(),
                    event == null ? null : event.getId(),
                    item.price(), item.type(), item.discount(), item.number(), venue.getId()
            ));
            context.ticketIds.add(ticket.id());
        }

        return new ImportResult(context.ticketIds, context.createdVenueIds,
                context.createdEventIds, context.createdPersonIds);
    }

    private Venue resolveVenue(VenueRequest input, int recordNumber, ImportContext context) {
        String key = normalizeName(input.name());
        Venue venue = context.venues.get(key);
        if (venue == null) {
            List<Venue> matches = venueService.findByName(input.name());
            if (matches.size() > 1) {
                throw TicketImportException.conflict(recordNumber, "venue.name",
                        "Several existing venues have this name");
            }
            if (matches.isEmpty()) {
                venue = venueService.create(new VenueRequest(
                        input.name().trim(), input.capacity(), input.type()));
                context.createdVenueIds.add(venue.getId());
            } else {
                venue = matches.get(0);
            }
            context.venues.put(key, venue);
        }

        if (venue.getCapacity() != input.capacity() || venue.getType() != input.type()) {
            throw TicketImportException.conflict(recordNumber, "venue",
                    "A venue with this name has different capacity or type");
        }
        return venue;
    }

    private Event resolveEvent(EventRequest input, int recordNumber, ImportContext context) {
        if (input == null) {
            return null;
        }

        EventKey key = new EventKey(normalizeName(input.name()), input.date());
        Event event = context.events.get(key);
        if (event == null) {
            List<Event> matches = eventService.findByNameAndDate(input.name(), input.date());
            if (matches.size() > 1) {
                throw TicketImportException.conflict(recordNumber, "event",
                        "Several existing events have this name and date/time");
            }
            if (matches.isEmpty()) {
                event = eventService.create(new EventRequest(
                        input.name().trim(), input.date(), input.minAge(),
                        input.ticketsCount(), input.description()));
                context.createdEventIds.add(event.getId());
            } else {
                event = matches.get(0);
            }
            context.events.put(key, event);
        }

        if (event.getMinAge() != input.minAge()
                || event.getTicketsCount() != input.ticketsCount()
                || !Objects.equals(event.getDescription(), input.description())) {
            throw TicketImportException.conflict(recordNumber, "event",
                    "An event with this name and date/time has different fields");
        }
        return event;
    }

    private Person resolvePerson(PersonRequest input, int recordNumber, ImportContext context) {
        if (input == null) {
            return null;
        }

        Person person = context.persons.get(input);
        if (person == null) {
            List<Person> matches = personService.findMatching(input);
            if (matches.size() > 1) {
                throw TicketImportException.conflict(recordNumber, "person",
                        "Several existing persons match all fields, including location");
            }
            if (matches.isEmpty()) {
                person = personService.create(input);
                context.createdPersonIds.add(person.getId());
            } else {
                person = matches.get(0);
            }
            context.persons.put(input, person);
        }
        return person;
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private record EventKey(String name, LocalDateTime date) {}

    private static class ImportContext {
        private final Map<String, Venue> venues = new HashMap<>();
        private final Map<EventKey, Event> events = new HashMap<>();
        private final Map<PersonRequest, Person> persons = new HashMap<>();
        private final List<Integer> ticketIds = new ArrayList<>();
        private final List<Long> createdVenueIds = new ArrayList<>();
        private final List<Long> createdEventIds = new ArrayList<>();
        private final List<Long> createdPersonIds = new ArrayList<>();
    }

    public record ImportResult(List<Integer> ticketIds, List<Long> createdVenueIds,
                               List<Long> createdEventIds, List<Long> createdPersonIds) {
        public ImportResult {
            ticketIds = List.copyOf(ticketIds);
            createdVenueIds = List.copyOf(createdVenueIds);
            createdEventIds = List.copyOf(createdEventIds);
            createdPersonIds = List.copyOf(createdPersonIds);
        }
    }
}
