package dat.entities;

import dat.dao.AudioFileDAO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audio_files")
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "filename")
    private String filename;

    private long sizeBytes;

    private LocalDateTime uploadedAt;
    @Column(name="bpm")
    private Double bpm;


    public AudioFile(String filename, long sizeBytes) {
        this.filename = filename;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = LocalDateTime.now();
    }


}
