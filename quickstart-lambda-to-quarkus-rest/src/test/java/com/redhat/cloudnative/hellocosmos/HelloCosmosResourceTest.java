
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@QuarkusTest
public class HelloCosmosResourceTest {  

    @Test
    public void testHelloEndpoint() {

        given()
          .when().get("/hello/Germany")
          .then()
             .statusCode(200);
    }

}