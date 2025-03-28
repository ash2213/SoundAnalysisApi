package dat.routes;

import dat.config.ApplicationConfig;
import dat.routes.RouteDefinitions;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class AudioEndpointsTest {

    private static ApplicationConfig app;

    @BeforeAll
    static void setup() {
       app= ApplicationConfig.getInstance()
                .initiateServer()
                .setRoute(RouteDefinitions.getRoutes()) // hvis du bruger ConnectionPool kan du mocke eller oprette test-db
                .handleException()
                .startServer(7002);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7002;
        RestAssured.basePath = "/api";
    }

    @AfterAll
    static void teardown() {
        app.stopServer();
    }

    @Test
    void getAllAudioFiles() {
        given()
                .when()
                .get("/audio/file")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("files", notNullValue());
    }


    @Test
    void showGraph_invalidId() {
        given()
                .when()
                .get("/audio/graph/999999")
                .then()
                .statusCode(anyOf(is(404), is(500)))
                .body("error", notNullValue());
    }
}
