package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.AnalysisResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public abstract class AbstractDAO<T> {
    protected EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private Class<T> entityClass;

    public AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    public void save(T entity) {
        EntityManager em = emf.createEntityManager();
        try {
            if (entity instanceof AnalysisResult r) {
                if (r.getResultData().matches("^\\d+$")) {
                    throw new RuntimeException("❌ Trying to save numeric-only resultData: " + r.getResultData());
                }
            }
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(T entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(T entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            if (id != null) {
                @SuppressWarnings("unchecked")
                Class<T> entityClass = (Class<T>) entity.getClass();
                T managed = em.find(entityClass, id);
                if (managed != null) {
                    em.remove(managed);
                }
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }



    public void merge(T entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);  // Merge will update the existing record
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
