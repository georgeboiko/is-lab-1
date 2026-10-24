package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.VenueRequest;
import itmo.is.lab1.model.Venue;
import itmo.is.lab1.repository.TicketRepository;
import itmo.is.lab1.repository.VenueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@ApplicationScoped
public class VenueService {

    @Inject
    private VenueRepository venueRepository;

    @Inject
    private TicketRepository ticketRepository;

    @Transactional
    public Venue create(@Valid @NotNull VenueRequest input) {
        Venue venue = new Venue(input.name(), input.capacity(), input.type());
        venueRepository.persist(venue);
        return venue;
    }

    public Venue findById(long id) {
        return requireVenue(id);
    }

    public List<Venue> findAll() {
        return venueRepository.findAll();
    }

    public List<Venue> findByName(String name) {
        return venueRepository.findByName(name);
    }

    @Transactional
    public Venue update(long id, @Valid @NotNull VenueRequest input) {
        Venue venue = requireVenue(id);
        venue.setName(input.name());
        venue.setCapacity(input.capacity());
        venue.setType(input.type());
        return venue;
    }

    @Transactional
    public void delete(long id) {
        Venue venue = requireVenue(id);
        if (ticketRepository.existsByVenueId(id)) {
            throw new IllegalStateException(
                    "Venue with id " + id + " is used by a ticket and cannot be deleted");
        }
        venueRepository.remove(venue);
    }

    private Venue requireVenue(long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Venue with id " + id + " was not found"));
    }
}
