package itmo.is.lab1.service;

import itmo.is.lab1.dto.request.CoordinatesRequest;
import itmo.is.lab1.dto.request.EventRequest;
import itmo.is.lab1.dto.request.LocationRequest;
import itmo.is.lab1.dto.request.PersonRequest;
import itmo.is.lab1.dto.request.TicketImportItem;
import itmo.is.lab1.dto.request.TicketImportRequest;
import itmo.is.lab1.dto.request.VenueRequest;
import itmo.is.lab1.model.Color;
import itmo.is.lab1.model.Country;
import itmo.is.lab1.model.TicketType;
import itmo.is.lab1.model.VenueType;
import itmo.is.lab1.service.error.TicketImportException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import jakarta.validation.Validator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TicketImportParser {

    public static final int MAX_FILE_BYTES = 5 * 1024 * 1024;

    @Inject
    private Validator validator;

    public TicketImportRequest parse(InputStream input) throws IOException {
        byte[] content = input.readNBytes(MAX_FILE_BYTES + 1);
        if (content.length > MAX_FILE_BYTES) {
            throw TicketImportException.invalid("file", "Maximum file size is 5 MiB");
        }

        try (JsonParser parser = Json.createParser(new ByteArrayInputStream(content))) {
            if (!parser.hasNext()) {
                throw TicketImportException.invalid("file", "JSON file is empty");
            }
            parser.next();
            JsonValue document = parser.getValue();
            if (parser.hasNext()) {
                throw TicketImportException.invalid("file", "Expected a single JSON document");
            }
            return readDocument(document);
        } catch (JsonParsingException exception) {
            throw TicketImportException.invalid("file",
                    "Invalid JSON at line " + exception.getLocation().getLineNumber()
                            + ", column " + exception.getLocation().getColumnNumber());
        } catch (NumberFormatException exception) {
            throw TicketImportException.invalid("file", "A JSON number is out of range");
        }
    }

    private TicketImportRequest readDocument(JsonValue document) {
        JsonObject root = object(document, "file");
        requireFields(root, Set.of("tickets"), "");
        JsonValue value = root.get("tickets");
        if (!(value instanceof JsonArray tickets)) {
            throw TicketImportException.invalid("tickets", "Expected an array");
        }
        if (tickets.isEmpty() || tickets.size() > TicketImportRequest.MAX_TICKETS) {
            throw TicketImportException.invalid("tickets",
                    "Expected between 1 and " + TicketImportRequest.MAX_TICKETS + " tickets");
        }

        List<TicketImportItem> items = new ArrayList<>(tickets.size());
        for (int i = 0; i < tickets.size(); ++i) {
            try {
                TicketImportItem item = readTicket(object(tickets.get(i), "ticket"));
                validate(item);
                items.add(item);
            } catch (TicketImportException exception) {
                throw exception.atRecord(i + 1);
            }
        }
        return new TicketImportRequest(items);
    }

    private TicketImportItem readTicket(JsonObject ticket) {
        requireFields(ticket, Set.of("name", "coordinates", "person", "event",
                "price", "type", "discount", "number", "venue"), "");
        return new TicketImportItem(
                string(ticket, "name", "name"),
                readCoordinates(optionalObject(ticket, "coordinates", "coordinates")),
                readPerson(optionalObject(ticket, "person", "person")),
                readEvent(optionalObject(ticket, "event", "event")),
                decimal(ticket, "price", "price"),
                enumeration(ticket, "type", "type", TicketType.class),
                decimal(ticket, "discount", "discount"),
                integer(ticket, "number", "number"),
                readVenue(optionalObject(ticket, "venue", "venue"))
        );
    }

    private CoordinatesRequest readCoordinates(JsonObject coordinates) {
        if (coordinates == null) {
            return null;
        }
        requireFields(coordinates, Set.of("x", "y"), "coordinates");
        return new CoordinatesRequest(
                integer(coordinates, "x", "coordinates.x"),
                longInteger(coordinates, "y", "coordinates.y"));
    }

    private PersonRequest readPerson(JsonObject person) {
        if (person == null) {
            return null;
        }
        requireFields(person, Set.of("eyeColor", "hairColor", "location",
                "height", "nationality"), "person");
        return new PersonRequest(
                enumeration(person, "eyeColor", "person.eyeColor", Color.class),
                enumeration(person, "hairColor", "person.hairColor", Color.class),
                readLocation(optionalObject(person, "location", "person.location")),
                integer(person, "height", "person.height"),
                enumeration(person, "nationality", "person.nationality", Country.class)
        );
    }

    private LocationRequest readLocation(JsonObject location) {
        if (location == null) {
            return null;
        }
        requireFields(location, Set.of("x", "y", "z", "name"), "person.location");
        return new LocationRequest(
                decimal(location, "x", "person.location.x"),
                integer(location, "y", "person.location.y"),
                longInteger(location, "z", "person.location.z"),
                string(location, "name", "person.location.name")
        );
    }

    private EventRequest readEvent(JsonObject event) {
        if (event == null) {
            return null;
        }
        requireFields(event, Set.of("name", "date", "minAge", "ticketsCount",
                "description"), "event");
        return new EventRequest(
                string(event, "name", "event.name"),
                dateTime(event, "date", "event.date"),
                longInteger(event, "minAge", "event.minAge"),
                integer(event, "ticketsCount", "event.ticketsCount"),
                string(event, "description", "event.description")
        );
    }

    private VenueRequest readVenue(JsonObject venue) {
        if (venue == null) {
            return null;
        }
        requireFields(venue, Set.of("name", "capacity", "type"), "venue");
        return new VenueRequest(
                string(venue, "name", "venue.name"),
                integer(venue, "capacity", "venue.capacity"),
                enumeration(venue, "type", "venue.type", VenueType.class)
        );
    }

    private void validate(TicketImportItem item) {
        validator.validate(item).stream()
                .findFirst()
                .ifPresent(violation -> {
                    throw TicketImportException.invalid(
                            violation.getPropertyPath().toString(),
                            violation.getMessage()
                    );
                });
    }

    private void requireFields(JsonObject object, Set<String> allowed, String path) {
        object.keySet().stream().filter(key -> !allowed.contains(key)).sorted()
                .findFirst().ifPresent(key -> {
                    throw TicketImportException.invalid(
                            path.isEmpty() ? key : path + "." + key, "Unknown field");
                });
    }

    private JsonObject object(JsonValue value, String field) {
        if (value instanceof JsonObject object) {
            return object;
        }
        throw TicketImportException.invalid(field, "Expected an object");
    }

    private JsonObject optionalObject(JsonObject parent, String key, String field) {
        JsonValue value = parent.get(key);
        return absent(value) ? null : object(value, field);
    }

    private String string(JsonObject object, String key, String field) {
        JsonValue value = object.get(key);
        if (absent(value)) {
            return null;
        }
        if (value instanceof JsonString string) {
            return string.getString();
        }
        throw TicketImportException.invalid(field, "Expected a string");
    }

    private JsonNumber number(JsonObject object, String key, String field) {
        JsonValue value = object.get(key);
        if (absent(value)) {
            return null;
        }
        if (value instanceof JsonNumber number) {
            return number;
        }
        throw TicketImportException.invalid(field, "Expected a number");
    }

    private Integer integer(JsonObject object, String key, String field) {
        JsonNumber number = number(object, key, field);
        if (number == null) {
            return null;
        }
        try {
            return number.intValueExact();
        } catch (ArithmeticException exception) {
            throw TicketImportException.invalid(field, "Expected a 32-bit integer");
        }
    }

    private Long longInteger(JsonObject object, String key, String field) {
        JsonNumber number = number(object, key, field);
        if (number == null) {
            return null;
        }
        try {
            return number.longValueExact();
        } catch (ArithmeticException exception) {
            throw TicketImportException.invalid(field, "Expected a 64-bit integer");
        }
    }

    private Double decimal(JsonObject object, String key, String field) {
        JsonNumber number = number(object, key, field);
        if (number == null) {
            return null;
        }
        double value = number.doubleValue();
        if (!Double.isFinite(value)) {
            throw TicketImportException.invalid(field, "Expected a finite double value");
        }
        return value;
    }

    private LocalDateTime dateTime(JsonObject object, String key, String field) {
        String value = string(object, key, field);
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw TicketImportException.invalid(field,
                    "Expected an ISO local date and time, for example 2026-11-15T19:00:00");
        }
    }

    private <E extends Enum<E>> E enumeration(JsonObject object, String key,
                                             String field, Class<E> type) {
        String value = string(object, key, field);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            throw TicketImportException.invalid(field,
                    "Expected one of " + Arrays.toString(type.getEnumConstants()));
        }
    }

    private boolean absent(JsonValue value) {
        return value == null || value.getValueType() == JsonValue.ValueType.NULL;
    }
}
