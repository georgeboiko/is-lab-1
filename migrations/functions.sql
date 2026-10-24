BEGIN;

CREATE OR REPLACE FUNCTION get_ticket_with_max_id()
RETURNS tickets
LANGUAGE sql
AS $$
    SELECT t.*
    FROM tickets AS t
    ORDER BY t.id DESC
    LIMIT 1;
$$;

CREATE OR REPLACE FUNCTION find_tickets_by_name_prefix(p_prefix TEXT)
RETURNS SETOF tickets
LANGUAGE sql
AS $$
    SELECT t.*
    FROM tickets AS t
    WHERE p_prefix IS NULL OR starts_with(t.name, p_prefix)
    ORDER BY t.id;
$$;

CREATE OR REPLACE FUNCTION find_tickets_by_venue_greater_than(p_capacity INTEGER)
RETURNS SETOF tickets
LANGUAGE sql
AS $$
    SELECT t.*
    FROM tickets AS t
    JOIN venues AS v ON v.id = t.venue_id
    WHERE v.capacity > p_capacity
    ORDER BY t.id;
$$;

CREATE OR REPLACE FUNCTION sell_ticket(
    p_ticket_id INTEGER,
    p_amount DOUBLE PRECISION,
    p_person_id BIGINT
)
RETURNS tickets
LANGUAGE sql
AS $$
    UPDATE tickets
    SET person_id = p_person_id,
        price = p_amount
    WHERE id = p_ticket_id
    RETURNING *;
$$;

CREATE OR REPLACE FUNCTION copy_ticket_with_discount(
    p_ticket_id INTEGER,
    p_discount DOUBLE PRECISION,
    p_number INTEGER
)
RETURNS tickets
LANGUAGE sql
AS $$
    INSERT INTO tickets (
        name,
        coordinates_x,
        coordinates_y,
        person_id,
        event_id,
        price,
        type,
        discount,
        number,
        venue_id
    )
    SELECT
        t.name,
        t.coordinates_x,
        t.coordinates_y,
        t.person_id,
        t.event_id,
        ROUND(
            t.price::NUMERIC * (1 + p_discount::NUMERIC / 100),
            2
        )::DOUBLE PRECISION,
        t.type,
        p_discount,
        p_number,
        t.venue_id
    FROM tickets AS t
    WHERE t.id = p_ticket_id
      AND t.price IS NOT NULL
    RETURNING *;
$$;

COMMIT;
