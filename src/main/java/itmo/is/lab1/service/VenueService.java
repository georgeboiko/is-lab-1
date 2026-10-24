package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.VenueRequest;
import itmo.is.lab1.model.Venue;
import itmo.is.lab1.repository.TicketRepository;
import itmo.is.lab1.repository.VenueRepository;
import itmo.is.lab1.service.error.UniquenessViolationException;
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
    private TransactionIsolation transactionIsolation;

    @Inject
    private VenueRepository venueRepository;

    @Inject
    private TicketRepository ticketRepository;

    @Transactional
    public Venue create(@Valid @NotNull VenueRequest input) {
        transactionIsolation.serializable();
        requireUniqueName(input.name(), null);
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
        transactionIsolation.serializable();
        Venue venue = requireVenue(id);
        requireUniqueName(input.name(), id);
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

    private void requireUniqueName(String name, Long excludedId) {
        for (Venue venue : venueRepository.findByName(name)) {
            if (excludedId == null || !excludedId.equals(venue.getId())) {
                throw new UniquenessViolationException("name",
                        "A venue with this name already exists");
            }
        }
    }

    private Venue requireVenue(long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Venue with id " + id + " was not found"));
    }
}
