package dat.controller;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BPMDetector {

    public double detectBPM(File audioFile) throws Exception {
        int bufferSize = 2048;
        int overlap = 512;

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
        List<Double> onsets = new ArrayList<>();

        OnsetHandler handler = (time, salience) -> {
            onsets.add(time);
            System.out.printf("🔹 Onset detected at %.2f s (salience: %.2f)\n", time, salience);
        };

        ComplexOnsetDetector detector = new ComplexOnsetDetector(bufferSize, overlap);
        detector.setHandler(handler);
        detector.setThreshold(0.1); // Du kan justere denne

        dispatcher.addAudioProcessor(detector);
        dispatcher.run();

        if (onsets.size() < 2) {
            System.out.println("⚠️ Ikke nok onsets – prøv en anden fil eller justér threshold.");
            return 0.0;
        }

        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < onsets.size(); i++) {
            intervals.add(onsets.get(i) - onsets.get(i - 1));
        }

        double avgInterval = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double bpm = 60.0 / avgInterval;

        System.out.printf("🎧 Beregnet BPM: %.2f\n", bpm);
        return bpm;
    }
}
