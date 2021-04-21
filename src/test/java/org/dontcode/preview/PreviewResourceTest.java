package org.dontcode.preview;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
        String testString = "{\"name\":\"pizza\"}";

        given()
                .contentType(ContentType.JSON)
                .body(testString)
                .when().post("/")
                .then()
                .statusCode(HttpStatus.SC_OK);
        Mockito.verify(previewSocket, Mockito.times(1)).broadcast(Mockito.anyString());

    }

}