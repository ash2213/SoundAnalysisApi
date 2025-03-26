package dat.dtos;

import dat.dtos.PitchPointDTO;
import dat.entities.AnalysisResult;
import java.util.List;

public class AnalysisResponseDTO {
    private final AnalysisResult result;
    private final List<PitchPointDTO> pitchPoints;

    public AnalysisResponseDTO(AnalysisResult result, List<PitchPointDTO> pitchPoints) {
        this.result = result;
        this.pitchPoints = pitchPoints;
    }

    public AnalysisResult getResult() {
        return result;
    }

    public List<PitchPointDTO> getPitchPoints() {
        return pitchPoints;
    }
}
