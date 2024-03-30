package net.dontcode.preview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import net.dontcode.common.session.SessionDetail;
import net.dontcode.common.session.SessionOverview;
import net.dontcode.common.session.SessionService;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(SessionResource.class)
public class SessionResourceTest {

    @InjectMock
    SessionService sessionService;

    @Test
    public void isAbleToListSessions() {
        Mockito.when(sessionService.listSessionOverview(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Multi.createFrom().items(
                new SessionOverview("a1", ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(2), true, 3),
                new SessionOverview("a2", ZonedDateTime.now().plusSeconds(4), ZonedDateTime.now().plusSeconds(6), false, 2),
                new SessionOverview("a3", ZonedDateTime.now().plusSeconds(8), ZonedDateTime.now().plusSeconds(10), false, 6)
                ));
        Response response = given()
                .when().get("/")
                .then()
                .statusCode(HttpStatus.SC_OK)
                        .contentType(ContentType.JSON).extract().response();
        // Verify that sessionService is being called
        Mockito.verify(sessionService, Mockito.times(1)).listSessionOverview(Mockito.isNull(),Mockito.isNull(), Mockito.isNull());

        Assertions.assertEquals(3, response.jsonPath().getList("$").size() );
    }


    @Test
    public void isAbleToGetSessionDetails() throws JsonProcessingException {

        Mockito.when(sessionService.getSession(Mockito.any())).thenReturn(Uni.createFrom().item(
                new SessionDetail("d1", ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(2), true, 3, fromJsonToMap("""
                        {
                            "creation": {
                                "name":"Test"
                            }
                        }
                        """)))
        );
        Response response = given()
                .accept(ContentType.JSON)
                .when().get("/{sessionId}", "d1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON).extract().response();

        // Verify that sessionService is being called
        Mockito.verify(sessionService, Mockito.times(1)).getSession(Mockito.eq("d1"));
        Assertions.assertEquals("d1",response.jsonPath().getString("id"));
        Assertions.assertEquals("Test",response.jsonPath().getString("content.creation.name"));

    }

    public static Map<String, Object> fromJsonToMap (String json) throws JsonProcessingException {
        // convert JSON string to Java Map
        Map<String, Object> map = new ObjectMapper().readValue(json, LinkedHashMap.class);
        return map;
    }

}
