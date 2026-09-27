package itmo.is.lab1.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.websocket.Session;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UpdateNotifier {

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    public void add(Session session) {
        sessions.add(session);
    }

    public void remove(Session session) {
        sessions.remove(session);
    }

    public void notify(String entity, String action, long id) {
        String message = Json.createObjectBuilder()
                .add("entity", entity)
                .add("action", action)
                .add("id", id)
                .build()
                .toString();

        sessions.removeIf(session -> !session.isOpen());
        sessions.forEach(session -> session.getAsyncRemote().sendText(message));
    }
}
