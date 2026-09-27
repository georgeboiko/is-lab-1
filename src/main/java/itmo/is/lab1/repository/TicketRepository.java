package itmo.is.lab1.repository;

import itmo.is.lab1.model.Ticket;
import itmo.is.lab1.model.TicketReadModel;
import itmo.is.lab1.repository.projection.TicketReadRow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class TicketRepository {

    private static final Map<String, String> STRING_COLUMNS = Map.ofEntries(
            Map.entry("name", "t.name"),
            Map.entry("type", "t.type"),
            Map.entry("person.eyeColor", "p.eye_color"),
            Map.entry("person.hairColor", "p.hair_color"),
            Map.entry("person.nationality", "p.nationality"),
            Map.entry("person.location.name", "l.name"),
            Map.entry("event.name", "e.name"),
            Map.entry("event.description", "e.description"),
            Map.entry("venue.name", "v.name"),
            Map.entry("venue.type", "v.type")
    );

    private static final String TICKET_READ_MODEL_SELECT = """
            SELECT
                t.id AS ticket_id,
                t.name AS ticket_name,
                t.coordinates_x AS coordinates_x,
                t.coordinates_y AS coordinates_y,
                t.creation_date AS creation_date,
                t.price AS price,
                t.type AS ticket_type,
                t.discount AS discount,
                t.number AS number,
                p.id AS person_id,
                p.eye_color AS eye_color,
                p.hair_color AS hair_color,
                p.height AS height,
                p.nationality AS nationality,
                l.x AS location_x,
                l.y AS location_y,
                l.z AS location_z,
                l.name AS location_name,
                e.id AS event_id,
                e.name AS event_name,
                e.event_date AS event_date,
                e.min_age AS min_age,
                e.tickets_count AS tickets_count,
                e.description AS event_description,
                v.id AS venue_id,
                v.name AS venue_name,
                v.capacity AS venue_capacity,
                v.type AS venue_type
            FROM %s AS t
            LEFT JOIN persons p ON p.id = t.person_id
            LEFT JOIN locations l ON l.person_id = p.id
            LEFT JOIN events e ON e.id = t.event_id
            JOIN venues v ON v.id = t.venue_id
            """;

    @PersistenceContext(unitName = "lab1")
    private EntityManager entityManager;

    public void persist(Ticket ticket) {
        entityManager.persist(ticket);
        entityManager.flush();
        entityManager.refresh(ticket);
    }

    public Optional<Ticket> findEntityById(int id) {
        return Optional.ofNullable(entityManager.find(Ticket.class, id));
    }

    public Optional<TicketReadModel> findReadModelById(int id) {
        Query query = ticketReadModelQuery("tickets", " WHERE t.id = ?1");
        query.setParameter(1, id);
        return ticketReadModels(query).stream().findFirst();
    }

    public List<TicketReadModel> findReadModelPage(int offset, int limit,
                                                   String filterField,
                                                   String filterValue,
                                                   String sortField,
                                                   boolean ascending) {
        String filter = normalizeFilter(filterValue);
        String filterColumn = column(filterField);
        String sortColumn = column(sortField);
        String suffix = (filter == null
                ? ""
                : " WHERE POSITION(?1 IN LOWER(" + filterColumn + ")) > 0")
                + " ORDER BY " + sortColumn + " "
                + (ascending ? "ASC" : "DESC") + " NULLS LAST, t.id ASC";

        Query query = ticketReadModelQuery("tickets", suffix)
                .setFirstResult(offset)
                .setMaxResults(limit);

        if (filter != null) {
            query.setParameter(1, filter);
        }

        return ticketReadModels(query);
    }

    public long count(String filterField, String filterValue) {
        String filter = normalizeFilter(filterValue);
        String queryText = """
                SELECT COUNT(t.id)
                FROM tickets t
                LEFT JOIN persons p ON p.id = t.person_id
                LEFT JOIN locations l ON l.person_id = p.id
                LEFT JOIN events e ON e.id = t.event_id
                JOIN venues v ON v.id = t.venue_id
                """ + (filter == null
                ? ""
                : " WHERE POSITION(?1 IN LOWER(" + column(filterField) + ")) > 0");
        Query query = entityManager.createNativeQuery(queryText);

        if (filter != null) {
            query.setParameter(1, filter);
        }

        return ((Number) query.getSingleResult()).longValue();
    }

    public boolean existsByPersonId(long personId) {
        return entityManager.createQuery(
                        "SELECT COUNT(t) FROM Ticket t WHERE t.person.id = :id",
                        Long.class)
                .setParameter("id", personId)
                .getSingleResult() > 0;
    }

    public boolean existsByEventId(long eventId) {
        return entityManager.createQuery(
                        "SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :id",
                        Long.class)
                .setParameter("id", eventId)
                .getSingleResult() > 0;
    }

    public boolean existsByVenueId(long venueId) {
        return entityManager.createQuery(
                        "SELECT COUNT(t) FROM Ticket t WHERE t.venue.id = :id",
                        Long.class)
                .setParameter("id", venueId)
                .getSingleResult() > 0;
    }

    public void remove(Ticket ticket) {
        entityManager.remove(ticket);
    }

    public Optional<TicketReadModel> findWithMaxId() {
        Query query = ticketReadModelQuery(
                "get_ticket_with_max_id()", " ORDER BY t.id");
        return ticketReadModels(query).stream().findFirst();
    }

    public List<TicketReadModel> findByNamePrefix(String prefix) {
        Query query = ticketReadModelQuery(
                "find_tickets_by_name_prefix(?1)", " ORDER BY t.id");
        query.setParameter(1, prefix);
        return ticketReadModels(query);
    }

    public List<TicketReadModel> findByVenueCapacityGreaterThan(int capacity) {
        Query query = ticketReadModelQuery(
                "find_tickets_by_venue_greater_than(?1)", " ORDER BY t.id");
        query.setParameter(1, capacity);
        return ticketReadModels(query);
    }

    public Optional<TicketReadModel> sell(int ticketId, double amount, long personId) {
        Query query = ticketReadModelQuery(
                "sell_ticket(?1, ?2, ?3)", " ORDER BY t.id");
        query.setParameter(1, ticketId);
        query.setParameter(2, amount);
        query.setParameter(3, personId);
        return ticketReadModels(query).stream().findFirst();
    }

    public Optional<TicketReadModel> copyWithDiscount(int ticketId, double discount) {
        Query query = ticketReadModelQuery(
                "copy_ticket_with_discount(?1, ?2)", " ORDER BY t.id");
        query.setParameter(1, ticketId);
        query.setParameter(2, discount);
        return ticketReadModels(query).stream().findFirst();
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String column(String field) {
        String column = STRING_COLUMNS.get(field);
        if (column == null) {
            throw new IllegalArgumentException(
                    "Unsupported string field: " + field);
        }
        return column;
    }

    private Query ticketReadModelQuery(String source, String suffix) {
        return entityManager.createNativeQuery(
                TICKET_READ_MODEL_SELECT.formatted(source) + suffix,
                "TicketReadRowMapping");
    }

    private List<TicketReadModel> ticketReadModels(Query query) {
        List<?> rows = query.getResultList();
        return rows.stream()
                .map(TicketReadRow.class::cast)
                .map(TicketReadRow::toReadModel)
                .toList();
    }
}
