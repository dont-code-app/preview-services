package org.dontcode.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/preview")
@ApplicationScoped
public class PreviewSocket {
    private static Logger log = LoggerFactory.getLogger(PreviewSocket.class);

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        log.info("Session opened");
        sessions.put(String.valueOf(System.currentTimeMillis()), session);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Session closed");
        String key[] = new String[1];
        sessions.entrySet().forEach(stringSessionEntry -> {
            if (stringSessionEntry.getValue()==session)
                key[0]=stringSessionEntry.getKey();
        });
        sessions.remove(key[0]);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("Message Received");
        log.trace("{}", message);
        //broadcast(">> " + message);
    }

    public void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    log.error("Unable to send message: {}", result.getException());
                }
            });
        });
    }
}
