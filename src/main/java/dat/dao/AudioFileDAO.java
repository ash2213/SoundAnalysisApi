package dat.dao;

import dat.entities.AudioFile;
import dat.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import java.util.List;

public class AudioFileDAO extends AbstractDAO<AudioFile> {
    public AudioFileDAO() {
        super(AudioFile.class);
    }

    public AudioFile findByFileName(String fileName) {
        EntityManager em = emf.createEntityManager();
        try {
            List<AudioFile> results = em.createQuery(
                            "SELECT a FROM AudioFile a WHERE a.filename = :fileName", AudioFile.class)
                    .setParameter("fileName", fileName)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }




}

