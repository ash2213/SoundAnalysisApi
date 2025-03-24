package dat.service;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioAnalysisService {

    public String analyzeFile(File audioFile) {
        System.out.println("🎧 Analyzing file: " + audioFile.getAbsolutePath());

        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new RuntimeException("🚫 Invalid file");
        }

        try {
            // Basic audio dispatcher
            final int bufferSize = 2048;
            final int overlap = 1024;
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);

            StringBuilder pitchResults = new StringBuilder();
            PitchDetectionHandler handler = (PitchDetectionResult result, AudioEvent e) -> {
                float pitch = result.getPitch();
                if (pitch != -1) {
                    pitchResults.append(String.format("%.2f Hz\n", pitch));
                }
            };

            dispatcher.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    dispatcher.getFormat().getSampleRate(),
                    bufferSize,
                    handler
            ));

            // Run the dispatcher
            dispatcher.run();

            String analysis = "🎵 Pitch values:\n" + pitchResults;
            System.out.println(analysis);
            return analysis;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error analyzing audio: " + e.getMessage();
        }
    }


    public double analyzeBPM(File audioFile) throws Exception {
        List<Double> onsetTimes = new ArrayList<>();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, 2048, 1024);

        ComplexOnsetDetector onsetDetector = new ComplexOnsetDetector(2048, 1024, 44100);
        onsetDetector.setHandler(new OnsetHandler() {
            @Override
            public void handleOnset(double time, double salience) {
                onsetTimes.add(time);
            }
        });

        dispatcher.addAudioProcessor(onsetDetector);
        dispatcher.run();

        // Calculate intervals between onsets
        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < onsetTimes.size(); i++) {
            intervals.add(onsetTimes.get(i) - onsetTimes.get(i - 1));
        }

        // Calculate average interval
        double avgInterval = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double bpm = avgInterval > 0 ? 60.0 / avgInterval : 0.0;

        return Math.round(bpm * 100.0) / 100.0;  // Round to 2 decimal places
    }

}
