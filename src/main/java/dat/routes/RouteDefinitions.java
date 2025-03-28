package dat.routes;

import dat.controller.UserController;
import dat.controller.AudioController;
import dat.dao.AudioFileDAO;
import dat.dao.AnalysisResultDAO;
import dat.enums.Role;
import dat.service.AudioAnalysisService;
import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import org.postgresql.jdbc2.optional.ConnectionPool;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteDefinitions {

    public static EndpointGroup getRoutes(ConnectionPool connectionPool) {

        var emf = HibernateConfig.getEntityManagerFactory();
        AudioFileDAO audioFileDAO = new AudioFileDAO();
        AnalysisResultDAO analysisResultDAO = new AnalysisResultDAO();
        AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

        AudioController audioController = new AudioController(audioAnalysisService, audioFileDAO, analysisResultDAO);
        UserController userController = new UserController();

        return () -> {
            path("/audio", () -> {
                post("/upload", audioController::uploadAudio,Role.USER);
                get("/file", audioController::getAllAudioFiles,Role.ANYONE);
                get("/result", audioController::getAllAnalysisResults,Role.ANYONE);
                get("/graph/{id}", audioController::showGraph, Role.ANYONE);
            });
            path("/user", () -> {
                post("/register", ctx -> userController.createUser(ctx, connectionPool), Role.ANYONE);
                post("/login", ctx -> userController.login(ctx, connectionPool), Role.USER);
              //  get("/logout", ctx -> userController.logout(ctx)); // ✅ Fixed
            });

            get("/", userController::renderHomePage);

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