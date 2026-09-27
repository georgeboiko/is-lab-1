package itmo.is.lab1.api;

import itmo.is.lab1.service.AuthService;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.List;

@ServerEndpoint("/api/updates")
public class UpdatesEndpoint {

    @Inject
    private UpdateNotifier updateNotifier;

    @Inject
    private AuthService authService;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        String token = session.getRequestParameterMap()
                .getOrDefault("token", List.of())
                .stream()
                .findFirst()
                .orElse(null);
        if (authService.authenticate(token).isEmpty()) {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    "A valid token is required"));
            return;
        }
        updateNotifier.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        updateNotifier.remove(session);
    }
}
