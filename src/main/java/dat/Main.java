package dat;

import dat.config.ApplicationConfig;
import dat.routes.RouteDefinitions;
import org.postgresql.jdbc2.optional.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
      ConnectionPool connectionPool = new ConnectionPool();

        int port = 7001;

        ApplicationConfig.getInstance()
                .initiateServer()
                .setRoute(RouteDefinitions.getRoutes(connectionPool)) // ✅ Pass connectionPool
                .handleException()
                .startServer(port);

        logger.info("🚀 Server started on http://localhost:" + port + "/api");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 Shutting down server...");
            ApplicationConfig.stopServer();

        }));

    }
}