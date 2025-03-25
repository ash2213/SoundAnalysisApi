package dat.service;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import dat.controller.BPMDetector;
import dat.dao.AnalysisResultDAO;
import dat.dao.AudioFileDAO;
import dat.dtos.AnalysisResponseDTO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import dat.exceptions.ApiException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AudioAnalysisService {

    public AnalysisResponseDTO analyzeFile(File audioFile) {
        System.out.println("🎧 Analyzing file: " + audioFile.getAbsolutePath());

        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new RuntimeException("🚫 Invalid file");
        }

        try {
            final int bufferSize = 2048;
            final int overlap = 1024;
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);

            List<Double> pitchValues = new ArrayList<>();
            PitchDetectionHandler handler = (PitchDetectionResult result, AudioEvent e) -> {
                float pitch = result.getPitch();
                if (pitch > 50 && pitch < 4000) {
                    pitchValues.add((double) pitch);
                }
            };

            dispatcher.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    dispatcher.getFormat().getSampleRate(),
                    bufferSize,
                    handler
            ));

            dispatcher.run();

            List<Double> smoothed = smoothData(pitchValues, 5);
            String pitchDataString = smoothed.stream()
                    .map(p -> String.format("%.2f", p))
                    .reduce((p1, p2) -> p1 + "," + p2)
                    .orElse("NO_DATA");

            // DB: Gem AudioFile + BPM + AnalysisResult (som før)
            AudioFileDAO audioFileDAO = new AudioFileDAO();
            AnalysisResultDAO analysisResultDAO = new AnalysisResultDAO();

            AudioFile audioFileEntity = audioFileDAO.findByFileName(audioFile.getName());
            if (audioFileEntity == null) {
                audioFileEntity = new AudioFile(audioFile.getName(), audioFile.length());
                audioFileDAO.save(audioFileEntity);
            }

            BPMDetector bpmDetector = new BPMDetector();
            double bpm = bpmDetector.detectBPM(audioFile);
            audioFileEntity.setBpm(bpm);
            audioFileDAO.merge(audioFileEntity);

            AnalysisResult result = analysisResultDAO.findByAudioFileId(audioFileEntity.getId());
            if (result != null) {
                result.setResultData(pitchDataString);
                result.setAnalyzedAt(LocalDateTime.now());
                analysisResultDAO.merge(result);
            } else {
                result = AnalysisResult.fromPitchData(audioFileEntity, pitchDataString);
                analysisResultDAO.save(result);
            }

            return new AnalysisResponseDTO(result, smoothed);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Kunne ikke analysere lydfil: " + e.getMessage());
        }
    }

    private List<Double> smoothData(List<Double> data, int windowSize) {
        List<Double> smoothed = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - windowSize);
            int end = Math.min(data.size() - 1, i + windowSize);
            List<Double> window = data.subList(start, end + 1);
            double avg = window.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            smoothed.add(avg);
        }
        return smoothed;
    }
}
