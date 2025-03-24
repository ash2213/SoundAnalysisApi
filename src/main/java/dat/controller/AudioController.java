package dat.controller;

import dat.dao.AnalysisResultDAO;
import dat.dao.AudioFileDAO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

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


    public void analyzeSingleAudioFile(Context ctx) {
        try {
            Long fileId = Long.parseLong(ctx.pathParam("id"));  // e.g., /analyze/1
            AudioFile audioFile = audioFileDAO.findById(fileId);

            if (audioFile == null) {
                ctx.status(404).result("Audio file not found.");
                return;
            }

            // Construct path to the saved file
            Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), audioFile.getFilename());
            File file = filePath.toFile();

            if (!file.exists()) {
                ctx.status(404).result("Audio file not found in temp directory.");
                return;
            }

            // Analyze the file
            String result = audioAnalysisService.analyzeFile(file);

            // Save result to DB
            AnalysisResult analysisResult = new AnalysisResult(audioFile, result);
            analysisResultDAO.save(analysisResult);

            // Return result
            ctx.json(Map.of(
                    "file", audioFile.getFilename(),
                    "result", result
            ));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid file ID.");
        } catch (Exception e) {
            logger.error("Error analyzing file", e);
            ctx.status(500).result("Internal Server Error");
        }
    }


    public void analyzeAllAudioFiles(Context ctx) {
        try {
            List<AudioFile> audioFiles = audioFileDAO.findAll();
            List<Map<String, Object>> analysisResults = new ArrayList<>();

            for (AudioFile audioFile : audioFiles) {
                // Construct path to the saved file
                Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), audioFile.getFilename());
                File file = filePath.toFile();

                if (!file.exists()) {
                    logger.warn("File {} not found in temp dir, skipping...", audioFile.getFilename());
                    continue;
                }

                // Analyze the file
                String result = audioAnalysisService.analyzeFile(file);

                // Save result to DB
                AnalysisResult analysisResult = new AnalysisResult(audioFile, result);
                analysisResultDAO.save(analysisResult);

                // Collect for response
                analysisResults.add(Map.of(
                        "file", audioFile.getFilename(),
                        "result", result
                ));
            }

            ctx.json(Map.of(
                    "status", "success",
                    "analyzed_files", analysisResults
            ));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke analysere filer"));
        }
    }


    public void uploadAudio(Context ctx) {
        try {
            UploadedFile uploadedFile = ctx.uploadedFile("audio");
            if (uploadedFile == null) {
                ctx.status(400).json(Map.of("error", "Ingen fil uploadet"));
                return;
            }

            String fileName = uploadedFile.filename();
            long fileSize = uploadedFile.size();

            // Save temp file
            InputStream fileContent = uploadedFile.content();
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
            Files.copy(fileContent, tempPath, StandardCopyOption.REPLACE_EXISTING);
            File audioFile = tempPath.toFile();

            // Save metadata to DB
            AudioFile audioFileEntity = new AudioFile(fileName, fileSize);

            // ✅ BPM Detection
            BPMDetector bpmDetector = new BPMDetector();
            double bpm = bpmDetector.detectBPM(audioFile);
            audioFileEntity.setBpm(bpm);

            audioFileDAO.save(audioFileEntity);

            // ✅ Pitch Analysis
            String analysisResult = audioAnalysisService.analyzeFile(audioFile);

            // ✅ Save analysis result
            AnalysisResult result = new AnalysisResult(audioFileEntity, analysisResult);
            analysisResultDAO.save(result);

            // ✅ Send back response
            ctx.json(Map.of(
                    "status", "success",
                    "file", fileName,
                    "bpm", bpm,
                    "message", analysisResult
            ));

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
            // Get the audio file ID from the path parameter
            Long audioFileId = ctx.pathParamAsClass("id", Long.class).get();

            // Get the limit from query parameters (default to 100 if not provided)
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(100);
            String startTime = ctx.queryParam("startTime"); // Optional: Filter by start time
            String endTime = ctx.queryParam("endTime");   // Optional: Filter by end time

            // Fetch results for the specific audio file
            List<AnalysisResult> results;
            if (startTime != null && endTime != null) {
                results = analysisResultDAO.findByAudioFileIdAndTimeRange(audioFileId, startTime, endTime, limit);
            } else {
                results = analysisResultDAO.findLatestByAudioFile(audioFileId, limit);
            }

            // Create a dataset for the graph
            XYSeries series = new XYSeries("Pitch Data for Song ID: " + audioFileId);
            for (AnalysisResult result : results) {
                // Clean the resultData string
                String cleanedResultData = cleanResultData(result.getResultData());

                // Split the cleaned string into individual pitch values
                String[] pitchValues = cleanedResultData.split(",");
                for (int i = 0; i < pitchValues.length; i++) {
                    try {
                        double pitchValue = Double.parseDouble(pitchValues[i].trim());
                        series.add(i, pitchValue); // X-axis: index, Y-axis: pitch value
                    } catch (NumberFormatException e) {
                        // Log invalid pitch values (optional)
                        System.err.println("Invalid pitch value: " + pitchValues[i]);
                    }
                }
            }

            // Create the dataset
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(series);

            // Create the chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Pitch Data Over Time for Song ID: " + audioFileId, // Chart title
                    "Time (Index)",         // X-axis label
                    "Pitch (Hz)",          // Y-axis label
                    dataset,                // Data
                    PlotOrientation.VERTICAL,
                    true,                   // Include legend
                    true,
                    false
            );

            // Save the chart as an image
            File chartFile = new File("pitch_graph.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

            // Send the image as a response
            ctx.contentType("image/png");
            ctx.result(Files.readAllBytes(chartFile.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Kunne ikke generere graf"));
        }
    }
    private String cleanResultData(String resultData) {
        // Remove all non-numeric characters (except commas and periods)
        return resultData.replaceAll("[^\\d.,]", "");
    }
}
