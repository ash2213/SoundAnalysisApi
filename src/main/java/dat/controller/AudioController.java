package dat.controller;

import dat.dao.AnalysisResultDAO;
import dat.dao.AudioFileDAO;
import dat.dtos.AnalysisResponseDTO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import dat.exceptions.ApiException;
import dat.service.AudioAnalysisService;
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

            // 🔍 Analyse + pitch values
            AnalysisResponseDTO response = audioAnalysisService.analyzeFile(audioFile);
            AnalysisResult result = response.getResult();
            List<Double> pitchValues = response.getSmoothedPitchValues();

            // 📈 Lav graf direkte
            JFreeChart chart = createChartFromPitchList(pitchValues);
            File chartFile = new File("pitch_graph.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

            // 📤 Send grafen og data
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
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(100);

            List<AnalysisResult> results = analysisResultDAO.findLatestByAudioFile(audioFileId, limit);

            XYSeries series = new XYSeries("Pitch Data");
            for (AnalysisResult result : results) {
                String[] values = result.getResultData().split(",");
                for (int i = 0; i < values.length; i++) {
                    try {
                        double pitch = Double.parseDouble(values[i].trim());
                        if (pitch >= 20 && pitch <= 4000) {
                            series.add(i, pitch);
                        }
                    } catch (NumberFormatException ignore) {}
                }
            }

            XYSeriesCollection dataset = new XYSeriesCollection(series);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Pitch Graph", "Time", "Pitch (Hz)",
                    dataset, PlotOrientation.VERTICAL, false, true, false
            );

            File chartFile = new File("pitch_graph.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

            ctx.contentType("image/png");
            ctx.result(Files.readAllBytes(chartFile.toPath()));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke generere graf"));
        }
    }


    private JFreeChart createChartFromPitchList(List<Double> pitchValues) {
        XYSeries series = new XYSeries("Pitch");
        for (int i = 0; i < pitchValues.size(); i++) {
            double pitch = pitchValues.get(i);
            if (pitch >= 20 && pitch <= 4000) {
                series.add(i, pitch);
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return ChartFactory.createXYLineChart(
                "Live Pitch Graph",
                "Time (Index)",
                "Pitch (Hz)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }

}
