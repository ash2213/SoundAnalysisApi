package dat.controller;

import dat.dao.AnalysisResultDAO;
import dat.dao.AudioFileDAO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import dat.service.AudioAnalysisService;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AudioController {
    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private final AudioAnalysisService audioAnalysisService;
    private final AudioFileDAO audioFileDAO;
    private final AnalysisResultDAO analysisResultDAO;

    // Constructor
    public AudioController(AudioAnalysisService audioAnalysisService, AudioFileDAO audioFileDAO, AnalysisResultDAO analysisResultDAO) {
        this.audioAnalysisService = audioAnalysisService;
        this.audioFileDAO = audioFileDAO;
        this.analysisResultDAO = analysisResultDAO;
    }


    public void analyzeSingleAudioFile(Context ctx) {
        try {
            logger.info("Received request to analyze single file.");
            Long fileId = Long.parseLong(ctx.pathParam("id"));  // e.g., /analyze/1
            logger.info("Parsed file ID: {}", fileId);

            AudioFile audioFile = audioFileDAO.findById(fileId);
            if (audioFile == null) {
                ctx.status(404).result("Audio file not found.");
                return;
            }

            Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), audioFile.getFilename());
            File file = filePath.toFile();
            logger.info("Constructed file path: {}", filePath);

            if (!file.exists()) {
                ctx.status(404).result("Audio file not found in temp directory.");
                return;
            }

            logger.info("Starting analysis...");
            String result = audioAnalysisService.analyzeFile(file);
            logger.info("Analysis result: {}", result);

            AnalysisResult analysisResult = new AnalysisResult(audioFile, result);
            analysisResultDAO.save(analysisResult);
            logger.info("Saved analysis result to DB.");

            ctx.json(Map.of(
                    "file", audioFile.getFilename(),
                    "result", result
            ));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid file ID.");
        } catch (Exception e) {
            logger.error("Error analyzing file", e);  // Check console/log file for stack trace
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

            // Save metadata to DB
            AudioFile audioFileEntity = new AudioFile(fileName, fileSize);
            audioFileDAO.save(audioFileEntity);

            // ✅ Analyze the file with TarsosDSP
            String analysisResult = audioAnalysisService.analyzeFile(tempPath.toFile());

            // ✅ Save the analysis result to DB
            AnalysisResult result = new AnalysisResult(audioFileEntity, analysisResult);
            analysisResultDAO.save(result);  // Make sure this DAO is initialized

            // ✅ Send back response with analysis summary
            ctx.json(Map.of(
                    "status", "success",
                    "file", fileName,
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





}
