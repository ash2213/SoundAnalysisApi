package dat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audio_files")
@Getter
@Setter
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private long sizeBytes;

    private LocalDateTime uploadedAt;

    @Column(name = "bpm")
    private Double bpm;

    public AudioFile() {
        // Default constructor
    }

    public AudioFile(String filename, long sizeBytes) {
        this.filename = filename;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = LocalDateTime.now();
    }

}
