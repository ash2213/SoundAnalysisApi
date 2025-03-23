package dat.routes;

import dat.controller.AudioController;
import dat.dao.AudioFileDAO;
import dat.dao.AnalysisResultDAO;
import dat.service.AudioAnalysisService;
import dat.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteDefinitions {

    public static EndpointGroup getRoutes() {
        // 🎯 Initialize dependencies
        var emf = HibernateConfig.getEntityManagerFactory();
        AudioFileDAO audioFileDAO = new AudioFileDAO();
        AnalysisResultDAO analysisResultDAO = new AnalysisResultDAO();
        AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

        // 🎯 Pass everything into the controller
        AudioController audioController = new AudioController(audioAnalysisService, audioFileDAO, analysisResultDAO);

        return () -> {
            path("/audio", () -> {
                post("/upload", audioController::uploadAudio);
                get("/files", audioController::getAllAudioFiles);
                get("/analyze-all", audioController::analyzeAllAudioFiles);
                get("/results", audioController::getAllAnalysisResults);
                get("/analyze/{id}", audioController::analyzeSingleAudioFile);
                get("/graph/{id}", audioController::showGraph);
                // get("/graph/updates", audioController::streamGraphUpdates);


            });
        };
    }
}
