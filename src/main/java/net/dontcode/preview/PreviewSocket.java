package net.dontcode.preview;

import net.dontcode.core.Message;
import net.dontcode.session.SessionActionType;
import net.dontcode.session.SessionService;
import net.dontcode.websocket.MessageEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/preview", encoders = MessageEncoderDecoder.class, decoders = MessageEncoderDecoder.class)
@ApplicationScoped
public class PreviewSocket {
    private static Logger log = LoggerFactory.getLogger(PreviewSocket.class);

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    SessionService sessionService;

    @OnOpen
    public void onOpen(Session session) {
        log.info("Session opened");
        //sessions.put(String.valueOf(System.currentTimeMillis()), session);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Session closed");
        String key[] = new String[1];
        sessions.entrySet().forEach(stringSessionEntry -> {
            if (stringSessionEntry.getValue()==session)
                key[0]=stringSessionEntry.getKey();
        });
        if( key[0]!=null)
            sessions.remove(key[0]);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error in Session {} with message {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    public void onMessage(Message message, Session session) {
        log.debug("Message Received");
        log.trace("{}", message);
        if (message.getType().equals(Message.MessageType.INIT)) {
            sessions.put(message.getSessionId(), session);
            // As we know have the sessionId, there may be already some info in the database => We need to send them
            sessionService.listSessionsInOrder(message.getSessionId()).subscribe().with(readSession -> {
                if( readSession.type()== SessionActionType.UPDATE) {
                    broadcast(new Message(Message.MessageType.CHANGE, message.getSessionId(), readSession.change()));
                }
            }, throwable -> {
                log.error ("Error {} while sending existing session items to {}", throwable.getMessage(), message.getSessionId());
            });
        }
    }

    /**
     * Broadcast the message to the right client
     * @param message
     */
    public void broadcast(Message message) {
        if (message.getSessionId()!=null) {
            Session s = sessions.get(message.getSessionId());
            if (s!=null) {
                s.getAsyncRemote().sendObject(message, result ->  {
                    if (result.getException() != null) {
                        log.error("Unable to send message: {}", result.getException());
                    }
                });
                return;
            }
        }

        log.error ("Could not find client listening to sessionId {}", message.getSessionId());
    }
}
