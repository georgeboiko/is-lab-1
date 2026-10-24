package itmo.is.lab1.repository;

import itmo.is.lab1.model.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
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

    public List<Event> findByNameAndDate(String name, LocalDateTime date) {
        return entityManager.createQuery("""
                        SELECT e FROM Event e
                        WHERE LOWER(TRIM(e.name)) = LOWER(TRIM(:name))
                          AND e.date = :date
                        ORDER BY e.id
                        """, Event.class)
                .setParameter("name", name)
                .setParameter("date", date)
                .setMaxResults(2)
                .getResultList();
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
