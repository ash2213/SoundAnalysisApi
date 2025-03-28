package dat.dao;

import dat.DatabaseException;
import dat.config.HibernateConfig;
import dat.entities.Role;
import dat.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.postgresql.jdbc2.optional.ConnectionPool;
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
public class UserDAO {

    User user = new User();
    private static UserDAO instance;
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    public UserDAO() { }
    public UserDAO getInstance() {
        if(instance==null){
            instance = new UserDAO();
        }
        return instance;
    }

    public static void createUser(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12)); // Hash password

        String sql = "INSERT INTO users (email, password, is_admin) VALUES (?, ?, false)";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword); // Store hashed password
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("User creation failed: " + e.getMessage());
        }
    }

    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password"); // Get hashed password
                if (BCrypt.checkpw(password, storedHash)) { // Compare entered password with hashed password
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("email"),
                            storedHash,
                            rs.getBoolean("is_admin")
                    );
                }
            }
            throw new DatabaseException("Invalid email or password");

        } catch (SQLException e) {
            throw new DatabaseException("Login failed: " + e.getMessage());
        }
    }


    public User createUser(User user) {
        try(var em = emf.createEntityManager()){
            Set<Role> newRoleSet = new HashSet<>();
            if(Role.GetRoles().size() == 0){
                Role userRole = em.find(Role.class, "user");
                if(userRole == null){
                    userRole = new Role("user");
                    em.persist(userRole);
                }
                user.addRole(userRole);
            }
            user.getRoles().forEach(role->{
                Role foundRole = em.find(Role.class, role.getName());
                if(foundRole == null){
                    throw new EntityNotFoundException("No role found with that id");
                } else {
                    newRoleSet.add(foundRole);
                }
            });
            user.setRoles(newRoleSet);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch(Throwable ex){
            ex.printStackTrace();
        }
        return user;
    }

    public void createRole(Role role) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();

        }
    }


}
