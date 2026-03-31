package de.thi.mynd.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DemoResourceTest {

  @Test
  public void testDemoEndpointResponseIsValid() {
    given()
        .when()
        .get("/demo")
        .then()
        .statusCode(200)
        .contentType(MediaType.TEXT_PLAIN)
        .body(is("This is a demo controller"));
  }
}
