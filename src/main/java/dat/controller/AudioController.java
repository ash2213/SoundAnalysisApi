package dat.controller;

import dat.dao.AnalysisResultDAO;
import dat.dao.AudioFileDAO;
import dat.dtos.AnalysisResponseDTO;
import dat.dtos.PitchPointDTO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import dat.exceptions.ApiException;
import dat.service.AudioAnalysisService;
import dat.service.ChartUtilsHelper;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AudioController {
    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private final AudioAnalysisService audioAnalysisService;
    private final AudioFileDAO audioFileDAO;
    private final AnalysisResultDAO analysisResultDAO;

    public AudioController(AudioAnalysisService audioAnalysisService, AudioFileDAO audioFileDAO, AnalysisResultDAO analysisResultDAO) {
        this.audioAnalysisService = audioAnalysisService;
        this.audioFileDAO = audioFileDAO;
        this.analysisResultDAO = analysisResultDAO;
    }


    public void uploadAudio(Context ctx) {
        try {
            UploadedFile uploadedFile = ctx.uploadedFile("audio");
            if (uploadedFile == null) {
                ctx.status(400).json(Map.of("error", "Ingen fil uploadet"));
                return;
            }

            String fileName = uploadedFile.filename();
            InputStream fileContent = uploadedFile.content();
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
            Files.copy(fileContent, tempPath, StandardCopyOption.REPLACE_EXISTING);
            File audioFile = tempPath.toFile();

            // Analyse og pitch data med timestamp
            AnalysisResponseDTO response = audioAnalysisService.analyzeFile(audioFile);
            AnalysisResult result = response.getResult();
            List<PitchPointDTO> pitchPoints = response.getPitchPoints();

            // Generer graf
            JFreeChart chart = ChartUtilsHelper.createChartFromPitchPoints(pitchPoints);
            File chartFile = new File("pitch_graph.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

            // Send graf og info
            ctx.contentType("image/png");
            ctx.header("X-Audio-Info", "bpm=" + result.getAudioFile().getBpm());
            ctx.result(Files.readAllBytes(chartFile.toPath()));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke analysere lydfil"));
        }
    }




    public void getAllAudioFiles(Context ctx) {
        try {
            List<AudioFile> files = audioFileDAO.findAll();
            ctx.json(Map.of(
                    "status", "success",
                    "files", files
            ));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke hente lydfiler"));
        }
    }

    public void getAllAnalysisResults(Context ctx) {
        try {
            List<AnalysisResult> results = analysisResultDAO.findAll();
            ctx.json(Map.of(
                    "status", "success",
                    "results", results
            ));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke hente analyseresultater"));
        }
    }

    public void showGraph(Context ctx) {
        try {
            Long audioFileId = ctx.pathParamAsClass("id", Long.class).get();

            // Hent analysedata
            List<AnalysisResult> results = analysisResultDAO.findLatestByAudioFile(audioFileId, 1); // Én seneste analyse
            if (results.isEmpty()) {
                ctx.status(404).json(Map.of("error", "Ingen analyseresultater fundet for ID: " + audioFileId));
                return;
            }

            AnalysisResult result = results.get(0);
            String pitchData = result.getResultData();
            String[] pitchTokens = pitchData.split(",");

            // Hent audiofilens længde i sekunder
            AudioFile audioFile = audioFileDAO.findById(audioFileId);
            double durationInSeconds = getAudioFileDuration(audioFile.getFilename());

            int numPitches = pitchTokens.length;
            double timeStep = durationInSeconds / numPitches;

            XYSeries series = new XYSeries("Pitch");
            double minPitch = Double.MAX_VALUE;
            double maxPitch = Double.MIN_VALUE;

            for (int i = 0; i < pitchTokens.length; i++) {
                try {
                    double pitch = Double.parseDouble(pitchTokens[i].trim());
                    if (pitch >= 20 && pitch <= 4000) {
                        double time = i * timeStep;
                        series.add(time, pitch);
                        minPitch = Math.min(minPitch, pitch);
                        maxPitch = Math.max(maxPitch, pitch);
                    }
                } catch (NumberFormatException ignored) {}
            }

            // 📈 Lav graf
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Pitch Graph for Audio ID: " + audioFileId,
                    "Tid (sekunder)",
                    "Pitch (Hz)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );

            if (series.getItemCount() > 0) {
                chart.getXYPlot().getRangeAxis().setRange(minPitch - 10, maxPitch + 10);
            }

            File chartFile = new File("pitch_graph.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

            ctx.contentType("image/png");
            ctx.result(Files.readAllBytes(chartFile.toPath()));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke generere graf"));
        }
    }


    public double getAudioFileDuration(String filename) throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            return frames / format.getFrameRate();
        }
    }



}
