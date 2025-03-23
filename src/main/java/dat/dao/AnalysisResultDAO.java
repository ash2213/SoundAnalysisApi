package dat.dao;

import dat.entities.AnalysisResult;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class AnalysisResultDAO extends AbstractDAO<AnalysisResult> {
    public AnalysisResultDAO() {
        super(AnalysisResult.class);
    }

    public List<AnalysisResult> findByAudioFileId(Long audioFileId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :id", AnalysisResult.class)
                    .setParameter("id", audioFileId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<AnalysisResult> findLatestByAudioFile(Long audioFileId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<AnalysisResult> results = em.createQuery(
                            "SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :id ORDER BY ar.analyzedAt DESC",
                            AnalysisResult.class)
                    .setParameter("id", audioFileId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
}
