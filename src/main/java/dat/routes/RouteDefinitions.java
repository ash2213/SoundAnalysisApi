package dat.routes;

import dat.controller.UserController;
import dat.controller.AudioController;
import dat.dao.AudioFileDAO;
import dat.dao.AnalysisResultDAO;
import dat.enums.Role;
import dat.service.AudioAnalysisService;
import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;
public class RouteDefinitions {

    public static EndpointGroup getRoutes() {

        var emf = HibernateConfig.getEntityManagerFactory();
        AudioFileDAO audioFileDAO = new AudioFileDAO();
        AnalysisResultDAO analysisResultDAO = new AnalysisResultDAO();
        AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

        AudioController audioController = new AudioController(audioAnalysisService, audioFileDAO, analysisResultDAO);
        UserController userController = new UserController();

        return () -> {
            path("/audio", () -> {
                post("/upload", audioController::uploadAudio, Role.USER);
                get("/file", audioController::getAllAudioFiles, Role.ANYONE);
                get("/{id}", audioController::getAudioById, Role.ANYONE);
                get("/result", audioController::getAllAnalysisResults, Role.ANYONE);
                delete("/{id}", audioController::deleteAudio,Role.USER);
                get("/graph/{id}", audioController::showGraph, Role.ANYONE);
                put("/{id}", audioController::updateAudio, Role.USER);
            });
            path("/user", () -> {
                post("/register", userController::createUser, Role.ANYONE);
                post("/login", userController::login, Role.USER);
            });
        };
    }
}