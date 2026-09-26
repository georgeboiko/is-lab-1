package itmo.is.lab1.repository;

import itmo.is.lab1.model.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EventRepository {

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(Event event) {
        entityManager.persist(event);
    }

    public Optional<Event> findById(long id) {
        return Optional.ofNullable(entityManager.find(Event.class, id));
    }

    public List<Event> findAll() {
        return entityManager.createQuery(
                "SELECT e FROM Event e ORDER BY e.id", Event.class)
                .getResultList();
    }

    public void remove(Event event) {
        entityManager.remove(event);
    }
}
