package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.User;
import dat.exceptions.DatabaseException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import at.favre.lib.crypto.bcrypt.BCrypt;



public class UserDAO extends AbstractDAO<User> {
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public UserDAO() {
        super(User.class);
    }

    public static void createUser(String email, String password) throws DatabaseException {
        EntityManager em = emf.createEntityManager();
        try {
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(hashedPassword); // 👈 vigtigt at feltet matcher i User.java

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new DatabaseException("User creation failed: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public static User login(String email, String password) throws DatabaseException {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();

            // 👉 Brug det rigtige verify-kald
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
            if (!result.verified) {
                throw new DatabaseException("Invalid email or password");
            }

            return user;
        } catch (NoResultException e) {
            throw new DatabaseException("Invalid email or password");
        } catch (Exception e) {
            throw new DatabaseException("Login failed: " + e.getMessage());
        } finally {
            em.close();
        }
    }

}
