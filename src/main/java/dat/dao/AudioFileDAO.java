package dat.dao;

import dat.entities.AudioFile;
import dat.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import java.util.List;

public class AudioFileDAO extends AbstractDAO<AudioFile> {
    public AudioFileDAO() {
        super(AudioFile.class);
    }

    public AudioFile findByFileName(String fileName) throws ApiException {
        EntityManager em = emf.createEntityManager();
        try {
            List<AudioFile> results = em.createQuery(
                            "SELECT a FROM AudioFile a WHERE a.filename = :fileName", AudioFile.class)
                    .setParameter("fileName", fileName)
                    .getResultList();

            if (results.isEmpty()) {
                throw new ApiException(404, "File not found");
            }
            return results.get(0);
        } finally {
            em.close();
        }
    }
}

