package dat.dao;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import dat.exceptions.DatabaseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
class UserDAOTest {
    private static EntityManagerFactory emf;
    private static UserDAO userDAO;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine3.18")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("CONNECTION_STR", postgres.getJdbcUrl().replace(postgres.getDatabaseName(), ""));
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());

        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = new UserDAO();

        ApplicationConfig.getInstance()
                .initiateServer()
                .startServer(7002);
    }


    @BeforeEach
    void setup() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDownAll() {
        ApplicationConfig.stopServer();
        postgres.stop();
    }

    @Test
    void createUser_and_login_success() throws DatabaseException {
        String email = "test@example.com";
        String password = "securePassword123";

        UserDAO.createUser(email, password);
        User loggedIn = UserDAO.login(email, password);

        assertThat(loggedIn, is(notNullValue()));
        assertThat(loggedIn.getEmail(), is(email));
        assertThat(loggedIn.getUserId(), is(greaterThan(0)));
        assertThat(loggedIn.isAdmin(), is(false));
    }

    @Test
    void login_fails_with_wrong_password() throws dat.exceptions.DatabaseException {
        String email = "fail@example.com";
        String correctPassword = "correctPass";
        String wrongPassword = "wrongPass";

        UserDAO.createUser(email, correctPassword);

        DatabaseException exception = Assertions.assertThrows(DatabaseException.class, () -> {
            UserDAO.login(email, wrongPassword);
        });

        assertThat(exception.getMessage(), containsString("Invalid email or password"));
    }

    @Test
    void login_fails_with_unknown_user() {
        DatabaseException exception = Assertions.assertThrows(DatabaseException.class, () -> {
            UserDAO.login("nouser@example.com", "anyPassword");
        });

        assertThat(exception.getMessage(), containsString("Invalid email or password"));
    }
}
