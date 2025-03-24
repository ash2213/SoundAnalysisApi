package dat.controller;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BPMDetector {

    public double detectBPM(File audioFile) throws Exception {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, 2048, 512);
        List<Double> onsets = new ArrayList<>();

        OnsetHandler handler = (time, salience) -> onsets.add(time);

        ComplexOnsetDetector detector = new ComplexOnsetDetector(2048, 512);
        detector.setHandler(handler);

        dispatcher.addAudioProcessor(detector);
        dispatcher.run();

        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < onsets.size(); i++) {
            intervals.add(onsets.get(i) - onsets.get(i - 1));
        }

        if (intervals.isEmpty()) return 0.0;

        double avgInterval = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return 60.0 / avgInterval;
    }
}
