package dat.dao;

import dat.entities.AnalysisResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResultDAO extends AbstractDAO<AnalysisResult> {
    public AnalysisResultDAO() {
        super(AnalysisResult.class);
    }

    public void deleteByAudioFileId(Long audioFileId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM AnalysisResult ar WHERE ar.audioFile.id = :id")
                    .setParameter("id", audioFileId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public AnalysisResult findByAudioFileId(Long audioFileId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<AnalysisResult> results = em.createQuery(
                            "SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :audioFileId", AnalysisResult.class)
                    .setParameter("audioFileId", audioFileId)
                    .getResultList();

            if (results.isEmpty()) {
                return null;
            }
            return results.get(0);
        } finally {
            em.close();
        }
    }

    public List<AnalysisResult> findLatestByAudioFile(Long audioFileId, int limit) {
        return findAnalysisResultsByAudioFileAndTimeRange(audioFileId, null, null, limit);
    }

    public List<AnalysisResult> findByAudioFileIdAndTimeRange(Long audioFileId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return findAnalysisResultsByAudioFileAndTimeRange(audioFileId, startTime, endTime, limit);
    }

    private List<AnalysisResult> findAnalysisResultsByAudioFileAndTimeRange(Long audioFileId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            String queryStr = "SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :audioFileId";
            if (startTime != null && endTime != null) {
                queryStr += " AND ar.analyzedAt BETWEEN :startTime AND :endTime";
            }
            queryStr += " ORDER BY ar.analyzedAt DESC";

            TypedQuery<AnalysisResult> query = em.createQuery(queryStr, AnalysisResult.class)
                    .setParameter("audioFileId", audioFileId);

            if (startTime != null && endTime != null) {
                query.setParameter("startTime", startTime);
                query.setParameter("endTime", endTime);
            }

            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
