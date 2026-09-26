package itmo.is.lab1.repository;

import itmo.is.lab1.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class TicketRepository {

    private static final String SELECT_WITH_RELATIONS = """
            SELECT t
            FROM Ticket t
            LEFT JOIN FETCH t.person p
            LEFT JOIN FETCH p.location
            LEFT JOIN FETCH t.event
            JOIN FETCH t.venue
            """;

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(Ticket ticket) {
        entityManager.persist(ticket);
        entityManager.flush();
        entityManager.refresh(ticket);
    }

    public Optional<Ticket> findById(int id) {
        return entityManager.createQuery(
                        SELECT_WITH_RELATIONS + " WHERE t.id = :id", Ticket.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<Ticket> findPage(int offset, int limit, String nameFilter,
                                 boolean ascending) {
        String filter = normalizeFilter(nameFilter);
        String queryText = SELECT_WITH_RELATIONS
                + (filter == null ? "" : " WHERE LOWER(t.name) LIKE :filter")
                + " ORDER BY t.name " + (ascending ? "ASC" : "DESC") + ", t.id ASC";

        TypedQuery<Ticket> query = entityManager.createQuery(queryText, Ticket.class)
                .setFirstResult(offset)
                .setMaxResults(limit);

        if (filter != null) {
            query.setParameter("filter", filter);
        }

        return query.getResultList();
    }

    public long count(String nameFilter) {
        String filter = normalizeFilter(nameFilter);
        String queryText = "SELECT COUNT(t) FROM Ticket t"
                + (filter == null ? "" : " WHERE LOWER(t.name) LIKE :filter");
        TypedQuery<Long> query = entityManager.createQuery(queryText, Long.class);

        if (filter != null) {
            query.setParameter("filter", filter);
        }

        return query.getSingleResult();
    }

    public void remove(Ticket ticket) {
        entityManager.remove(ticket);
    }

    public Optional<Ticket> findWithMaxId() {
        return singleTicket(entityManager.createNativeQuery("""
                SELECT result.*
                FROM get_ticket_with_max_id() AS result
                WHERE result.id IS NOT NULL
                """, Ticket.class));
    }

    public List<Ticket> findByNamePrefix(String prefix) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM find_tickets_by_name_prefix(?1)", Ticket.class);
        query.setParameter(1, prefix);
        return ticketList(query);
    }

    public List<Ticket> findByVenueCapacityGreaterThan(int capacity) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM find_tickets_by_venue_greater_than(?1)", Ticket.class);
        query.setParameter(1, capacity);
        return ticketList(query);
    }

    public Optional<Ticket> sell(int ticketId, double amount, long personId) {
        Query query = entityManager.createNativeQuery("""
                SELECT result.*
                FROM sell_ticket(?1, ?2, ?3) AS result
                WHERE result.id IS NOT NULL
                """, Ticket.class);
        query.setParameter(1, ticketId);
        query.setParameter(2, amount);
        query.setParameter(3, personId);
        return singleTicket(query);
    }

    public Optional<Ticket> copyWithDiscount(int ticketId, double discount) {
        Query query = entityManager.createNativeQuery("""
                SELECT result.*
                FROM copy_ticket_with_discount(?1, ?2) AS result
                WHERE result.id IS NOT NULL
                """, Ticket.class);
        query.setParameter(1, ticketId);
        query.setParameter(2, discount);
        return singleTicket(query);
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private Optional<Ticket> singleTicket(Query query) {
        return ticketList(query).stream().findFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Ticket> ticketList(Query query) {
        return (List<Ticket>) query.getResultList();
    }
}
