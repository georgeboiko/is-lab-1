package itmo.is.lab1.api;

import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/api/updates")
public class UpdatesEndpoint {

    @Inject
    private UpdateNotifier updateNotifier;

    @OnOpen
    public void onOpen(Session session) {
        updateNotifier.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        updateNotifier.remove(session);
    }
}
