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
import dat.dtos.PitchPointDTO;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import dat.exceptions.ApiException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AudioAnalysisService {

    public AnalysisResponseDTO analyzeFile(File audioFile) {
        System.out.println(" Analyzing file: " + audioFile.getAbsolutePath());

        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new RuntimeException(" Invalid file");
        }

        try {
            final int bufferSize = 2048;
            final int overlap = 1024;

            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
            List<PitchPointDTO> pitchPoints = new ArrayList<>();

            PitchDetectionHandler handler = (PitchDetectionResult result, AudioEvent e) -> {
                float pitch = result.getPitch();
                if (pitch > 50 && pitch < 4000) {
                    pitchPoints.add(new PitchPointDTO(e.getTimeStamp(), pitch));
                }
            };

            dispatcher.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    dispatcher.getFormat().getSampleRate(),
                    bufferSize,
                    handler
            ));

            dispatcher.run();

            String pitchDataString = pitchPoints.stream()
                    .map(p -> String.format("%.2f", p.getPitch()))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

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
                result = new AnalysisResult();
                result.setAudioFile(audioFileEntity);
                result.setResultData(pitchDataString);
                result.setAnalyzedAt(LocalDateTime.now());
                analysisResultDAO.save(result);
            }

            return new AnalysisResponseDTO(result, pitchPoints);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Kunne ikke analysere lydfil: " + e.getMessage());
        }
    }
}
