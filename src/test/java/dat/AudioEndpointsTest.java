package dat.routes;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.routes.RouteDefinitions;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Testcontainers
class AudioEndpointsTest {

    // Starter en PostgreSQL-container automatisk
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_db")
            .withUsername("testuser")
            .withPassword("testpass");

    private static ApplicationConfig app;

    @BeforeAll
    static void setup() {
        // Fortæl Hibernate vi er i testmiljø
        HibernateConfig.setTest(true);

        // Start Javalin server med test-DB
        app = ApplicationConfig.getInstance()
                .initiateServer()
                .setRoute(RouteDefinitions.getRoutes())
                .handleException()
                .startServer(7002);

        // REST-assured opsætning
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
