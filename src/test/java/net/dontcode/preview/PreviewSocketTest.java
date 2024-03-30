package net.dontcode.preview;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import net.dontcode.core.Change;
import net.dontcode.core.DontCodeModelPointer;
import net.dontcode.core.Message;
import net.dontcode.common.session.SessionService;
import net.dontcode.common.websocket.MessageEncoderDecoder;
import org.apache.http.HttpStatus;
import org.bson.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(MongoTestProfile.class)
public class PreviewSocketTest extends AbstractMongoTest {

    @TestHTTPResource("/preview")
    URI uri;

    @TestHTTPResource("/messages")
    URI messagesUri;

    @Inject
    SessionService sessionService;

    public static String firstSessionId= UUID.randomUUID().toString();

    @Test
    public void testSession() throws DeploymentException, IOException, InterruptedException {
        ClientTestSession.receivedMessages.clear();
        ClientTestSession.opened=false;
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(ClientTestSession.class, uri)) {
            // Wait the data to be saved in the database
            for (int i = 0; i < 10; i++) {
                Thread.sleep(50);
                if( ClientTestSession.opened) {
                    break;
                }
            }
            Assertions.assertTrue(ClientTestSession.opened, "Session was not opened");

            Change chg = new Change(Change.ChangeType.RESET, "/", null);

                // First send a message for another previewer (sessionId is different)
            Message toSend = new Message(Message.MessageType.CHANGE, "345" ,chg);

            given()
                    .contentType(ContentType.JSON)
                    .body(toSend)
                    .when().post(messagesUri)
                    .then()
                    .statusCode(HttpStatus.SC_OK);

            Thread.sleep(50);
            // Then send a message for out previewer
            toSend = new Message(Message.MessageType.CHANGE, firstSessionId ,chg);

            given()
                    .contentType(ContentType.JSON)
                    .body(toSend)
                    .when().post(messagesUri)
                    .then()
                    .statusCode(HttpStatus.SC_OK);

            // Wait the message to be sent back to the client
            for (int i = 0; i < 10; i++) {
                Thread.sleep(50);
                if( ClientTestSession.receivedMessages.size()>=1) {
                    break;
                }
            }
            Assertions.assertEquals(1, ClientTestSession.receivedMessages.size(), "No Messages broadcasted or Message was not filtered per sesssionId");
        }
    }

    public static String otherSessionId = UUID.randomUUID().toString();

    @Test
    public void testLatePreview() throws DeploymentException, IOException, InterruptedException {
        OtherClientTestSession.receivedMessages.clear();
        OtherClientTestSession.opened=false;
        // First create sessions with a specific sessionId
        Change chg = new Change(Change.ChangeType.RESET, "/", null);

        sessionService.createNewSession(otherSessionId, "Test Session").await().indefinitely();
        sessionService.updateSession(otherSessionId, chg).await().indefinitely();

        var value = new JsonObject("""
                {
                    "name":"entityA"
                }""").toBsonDocument();
        var pointer = new DontCodeModelPointer("creation/entities/a", "creation/entities","creation", "creation", "a", Boolean.TRUE);
        chg = new Change(Change.ChangeType.RESET, "creation/entities/a", value, pointer);
        sessionService.updateSession(otherSessionId, chg).await().indefinitely();

        pointer = new DontCodeModelPointer("creation/entities/a/name", "creation/entities/name","creation/entities/a", "creation/entities", "name", Boolean.FALSE);
        chg = new Change(Change.ChangeType.ADD, "creation/entities/a/name", "NewNameA", pointer);
        sessionService.updateSession(otherSessionId, chg).await().indefinitely();

        value = new JsonObject("""
                {
                    "a": {
                        "name":"field1",
                        "type":"string"
                        },
                    "b": {
                        "name":"field2",
                        "type":"boolean"
                    }
                }""").toBsonDocument();
        pointer = new DontCodeModelPointer("creation/entities/a/fields", "creation/entities/fields","creation/entities/a", "creation/entities", "fields", null);
        chg = new Change(Change.ChangeType.RESET, "creation/entities/a/fields", value, pointer);
        sessionService.updateSession(otherSessionId, chg).await().indefinitely();

        OtherClientTestSession.opened=false;
        // Then connect the preview and it should receive all of the updates
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(OtherClientTestSession.class, uri)) {
            // Wait the data to be saved in the database
            for (int i = 0; i < 10; i++) {
                Thread.sleep(50);
                if( OtherClientTestSession.receivedMessages.size()>3) {
                    break;
                }
            }
            Assertions.assertTrue(OtherClientTestSession.opened, "Session was not opened");
            Assertions.assertEquals(4, OtherClientTestSession.receivedMessages.size(), "Not all previous messages received");
        }
    }

    @ClientEndpoint(encoders = MessageEncoderDecoder.class, decoders = MessageEncoderDecoder.class)
    public static class ClientTestSession {

        public static boolean opened=false;
        public static List<Message> receivedMessages = new Vector<>();
        protected String sessionId;

        public ClientTestSession () {
          sessionId=firstSessionId;
        }

        @OnOpen
        public void open(Session session) {
            //MESSAGES.add("CONNECT");
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendObject(new Message (Message.MessageType.INIT,sessionId));
            opened=true;
        }

        @OnMessage
        void message(Message msg) throws DecodeException {
            //MESSAGES.add(msg);
            //System.out.println(msg);
            receivedMessages.add(msg);
        }

        @OnError
        void error (Throwable error) {
            System.err.println("Error "+ error.getMessage());
        }

    }

    @ClientEndpoint(encoders = MessageEncoderDecoder.class, decoders = MessageEncoderDecoder.class)
    public static class OtherClientTestSession extends ClientTestSession {
        public OtherClientTestSession () {
            super();
            sessionId = otherSessionId;
        }
    }


}
