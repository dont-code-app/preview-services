package net.dontcode.preview;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import net.dontcode.core.Change;
import net.dontcode.core.DontCodeModelPointer;
import net.dontcode.core.Message;
import org.apache.http.HttpStatus;
import org.bson.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(FromIdeResource.class)
public class PreviewResourceTest {

    @InjectMock
    PreviewSocket previewSocket;

    @Test
    public void testIdeEndpoint() {
        Change chg = new Change(Change.ChangeType.RESET, "/", null);
        Message toSend = new Message(Message.MessageType.CHANGE, chg);

        given()
                .contentType(ContentType.JSON)
                .body(toSend)
                .when().post("/")
                .then()
                .statusCode(HttpStatus.SC_OK);
        // Verify that init messages are NOT broadcasted
        Mockito.verify(previewSocket, Mockito.times(1)).broadcast(Mockito.any(Message.class));


        var value = new JsonObject("""
                {
                    "type":"CHANGE",
                    "change": {
                        "type": "UPDATE",
                        "position": "creation/entities/a",
                        "value":{
                                "name":"entityA"
                            }
                        }
                }""").toBsonDocument();
        var pointer = new DontCodeModelPointer("creation/entities/a", "creation/entities","creation", "creation", null, "a");

        chg = new Change(Change.ChangeType.ADD, "creation/entities/a", value, pointer);
        toSend = new Message(Message.MessageType.CHANGE, chg);

        given()
                .contentType(ContentType.JSON)
                .body(toSend)
                .when().post("/")
                .then()
                .statusCode(HttpStatus.SC_OK);
        Mockito.verify(previewSocket, Mockito.times(2)).broadcast(Mockito.any(Message.class));


        pointer = new DontCodeModelPointer("creation/entities/a/name", "creation/entities/name","creation/entities/a", "creation/entities", "name", null);

        chg = new Change(Change.ChangeType.ADD, "creation/entities/a/name", "NewNameA", pointer);
        toSend = new Message(Message.MessageType.CHANGE, chg);

        given()
                .contentType(ContentType.JSON)
                .body(toSend)
                .when().post("/")
                .then()
                .statusCode(HttpStatus.SC_OK);
        Mockito.verify(previewSocket, Mockito.times(3)).broadcast(Mockito.any(Message.class));
    }

}