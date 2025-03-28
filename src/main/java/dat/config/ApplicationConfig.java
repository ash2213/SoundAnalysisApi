package dat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static io.javalin.apibuilder.ApiBuilder.path;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static ApplicationConfig applicationConfig;
    private static Javalin app;
    private static JavalinConfig javalinConfig;
    private boolean routesRegistered = false;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ApplicationConfig() {}

    public static ApplicationConfig getInstance() {
        if (applicationConfig == null) {
            applicationConfig = new ApplicationConfig();
        }
        return applicationConfig;
    }

    public static void stopServer() {
        app.stop();
    }


    public ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {

            javalinConfig = config;
            config.http.defaultContentType = "application/json; charset=utf-8";
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
        });

        return applicationConfig;
    }

    public ApplicationConfig setRoute(EndpointGroup route) {
        if (routesRegistered) {
            logger.error("Routes have already been registered.");
            throw new IllegalStateException("Routes have already been registered.");
        }
        logger.info("Registering routes...");
        javalinConfig.router.apiBuilder(() -> {
            path("/", route);
        });
        routesRegistered = true;
        return applicationConfig;
    }

    public ApplicationConfig startServer(int port) {
        app.start(port);
        return applicationConfig;
    }

    public ApplicationConfig handleException() {
        app.exception(Exception.class, (exception, ctx) -> {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("message", exception.getMessage());
            ctx.status(500);
            ctx.json(node);
        });
        return applicationConfig;
    }
}
