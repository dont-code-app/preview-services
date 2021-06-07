package net.dontcode.preview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import net.dontcode.core.Change;
import net.dontcode.core.Message;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(MongoTestProfile.class)
public class PreviewSocketTest extends AbstractMongoTest {

    @TestHTTPResource("/preview")
    URI uri;

    @TestHTTPResource("/messages")
    URI messagesUri;

    @Test
    public void testSession() throws DeploymentException, IOException, InterruptedException {
        //Mockito.when(previewService.receiveUpdate(Mockito.anyString())).thenThrow(new RuntimeException("Errorrrrererre"));//Return(Uni.createFrom().voidItem());
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
            // Then send a message for another previewer (sessionId is different)
            toSend = new Message(Message.MessageType.CHANGE, "123" ,chg);

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
            Assertions.assertEquals(ClientTestSession.receivedMessages.size(),1, "No Messages broadcasted or Message was not filtered per sesssionId");
        }
    }

    @ClientEndpoint
    public static class ClientTestSession {

        public static boolean opened=false;
        public static List<Message> receivedMessages = new Vector<>();

        @OnOpen
        public void open(Session session) {
            //MESSAGES.add("CONNECT");
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendText("""
                    {
                        "type":"INIT",
                        "sessionId":"123"
                    }""");
            opened=true;
        }

        @OnMessage
        void message(String msg) throws DecodeException {
            //MESSAGES.add(msg);
            //System.out.println(msg);
            receivedMessages.add(decode(msg));
        }

        @OnError
        void error (Throwable error) {
            System.err.println("Error "+ error.getMessage());
        }

        public Message decode(String text) throws DecodeException {
            ObjectMapper mapper = new ObjectMapper();
            Message obj = null;
            try {
                obj = mapper.readValue(text, Message.class);
            } catch (JsonProcessingException e) {
                throw new DecodeException(text, "Cannot decode Message", e);
            }
            return obj;
        }
    }


}