package dat.dtos;

import dat.entities.AnalysisResult;

import java.util.List;

public class AnalysisResponseDTO {
    private AnalysisResult result;
    private List<Double> smoothedPitchValues;

    public AnalysisResponseDTO(AnalysisResult result, List<Double> smoothedPitchValues) {
        this.result = result;
        this.smoothedPitchValues = smoothedPitchValues;
    }

    public AnalysisResult getResult() {
        return result;
    }

    public List<Double> getSmoothedPitchValues() {
        return smoothedPitchValues;
    }
}
