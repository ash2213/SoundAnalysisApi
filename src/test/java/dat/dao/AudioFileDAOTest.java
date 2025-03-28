package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.AudioFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AudioFileDAOTest {

    private EntityManagerFactory emf;
    private AudioFileDAO audioFileDAO;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    void setUpAll() {
        postgres.start();
        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        System.setProperty("CONNECTION_STR", postgres.getJdbcUrl().replace("jdbc:postgresql://", ""));
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        audioFileDAO = new AudioFileDAO();
    }

    @AfterEach
    void cleanUp() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM AudioFile").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    void tearDownAll() {
        postgres.stop();
    }

    @Test
    void testSaveAndFindByFileName() {
        AudioFile file = new AudioFile();
        file.setFilename("test.wav");
        file.setSizeBytes(1024);
        file.setUploadedAt(LocalDateTime.now());

        audioFileDAO.save(file);

        AudioFile result = audioFileDAO.findByFileName("test.wav");

        assertThat(result, is(notNullValue()));
        assertThat(result.getFilename(), is("test.wav"));
    }

    @Test
    void testFindByFileNameReturnsNullIfNotFound() {
        AudioFile result = audioFileDAO.findByFileName("nonexistent.wav");
        assertThat(result, is(nullValue()));
    }
}