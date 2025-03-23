package dat.dao;

import dat.entities.AnalysisResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
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

    public List<AnalysisResult> findLatestByAudioFile(Long audioFileId, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :audioFileId ORDER BY ar.analyzedAt DESC", AnalysisResult.class)
                    .setParameter("audioFileId", audioFileId)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<AnalysisResult> findByAudioFileIdAndTimeRange(Long audioFileId, String startTime, String endTime, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT ar FROM AnalysisResult ar WHERE ar.audioFile.id = :audioFileId AND ar.analyzedAt BETWEEN :startTime AND :endTime ORDER BY ar.analyzedAt DESC", AnalysisResult.class)
                    .setParameter("audioFileId", audioFileId)
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    public List<AnalysisResult> findLatest(int limit) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<AnalysisResult> query = em.createQuery(
                "SELECT a FROM AnalysisResult a ORDER BY a.analyzedAt DESC", AnalysisResult.class
        );
        query.setMaxResults(limit);
        return query.getResultList();
    }


    public List<AnalysisResult> findByTimeRange(String startTime, String endTime, int limit) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<AnalysisResult> query = em.createQuery(
                "SELECT a FROM AnalysisResult a WHERE a.analyzedAt BETWEEN :startTime AND :endTime ORDER BY a.analyzedAt DESC", AnalysisResult.class
        );
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
