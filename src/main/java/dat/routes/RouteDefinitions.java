package dat.routes;

import dat.controller.UserController;
import dat.controller.AudioController;
import dat.dao.AudioFileDAO;
import dat.dao.AnalysisResultDAO;
import dat.service.AudioAnalysisService;
import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import org.postgresql.jdbc2.optional.ConnectionPool;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteDefinitions {

    public static EndpointGroup getRoutes(ConnectionPool connectionPool) {

        // 🎯 Initialize dependencies
        var emf = HibernateConfig.getEntityManagerFactory();
        AudioFileDAO audioFileDAO = new AudioFileDAO();
        AnalysisResultDAO analysisResultDAO = new AnalysisResultDAO();
        AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

        // 🎯 Initialize controllers
        AudioController audioController = new AudioController(audioAnalysisService, audioFileDAO, analysisResultDAO);
        UserController userController = new UserController();

        return () -> {
            // 🎵 Audio-related routes
            path("/audio", () -> {
                post("/upload", audioController::uploadAudio);
                get("/file", audioController::getAllAudioFiles);
                get("/result", audioController::getAllAnalysisResults);
                get("/graph/{id}", audioController::showGraph);
            });

            // 👤 User authentication routes
            path("/user", () -> {
                post("/register", ctx -> userController.createUser(ctx, connectionPool));
                post("/login", ctx -> userController.login(ctx, connectionPool));
              //  get("/logout", ctx -> userController.logout(ctx)); // ✅ Fixed
            });

            // 🏠 Homepage route
            get("/", userController::renderHomePage);

            // 📊 Dashboard route (only accessible if logged in)
//            get("/dashboard", ctx -> {
//                if (ctx.sessionAttribute("isLoggedIn") == Boolean.TRUE) {
//                    userController.dashboard(ctx);
//                } else {
//                    ctx.redirect("/login");
//                }
            };//);
        };
    }
//}