package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "audio_file_id", referencedColumnName = "id")
    private AudioFile audioFile;

    @Lob
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    private LocalDateTime analyzedAt;

    public AnalysisResult() {}

    private AnalysisResult(AudioFile audioFile, String resultData) {
        this.audioFile = audioFile;
        this.resultData = resultData;
        this.analyzedAt = LocalDateTime.now();
    }

    public static AnalysisResult fromPitchData(AudioFile audioFile, String resultData) {
        return new AnalysisResult(audioFile, resultData);
    }

}
