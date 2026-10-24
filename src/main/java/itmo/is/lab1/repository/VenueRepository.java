package itmo.is.lab1.repository;

import itmo.is.lab1.model.Venue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VenueRepository {

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(Venue venue) {
        entityManager.persist(venue);
    }

    public Optional<Venue> findById(long id) {
        return Optional.ofNullable(entityManager.find(Venue.class, id));
    }

    public List<Venue> findByName(String name) {
        return entityManager.createQuery("""
                        SELECT v FROM Venue v
                        WHERE LOWER(TRIM(v.name)) = LOWER(TRIM(:name))
                        ORDER BY v.id
                        """, Venue.class)
                .setParameter("name", name)
                .setMaxResults(2)
                .getResultList();
    }

    public List<Venue> findAll() {
        return entityManager.createQuery(
                        "SELECT v FROM Venue v ORDER BY v.id", Venue.class)
                .getResultList();
    }

    public void remove(Venue venue) {
        entityManager.remove(venue);
    }
}
