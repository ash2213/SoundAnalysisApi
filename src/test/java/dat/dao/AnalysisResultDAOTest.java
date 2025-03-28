package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.AnalysisResult;
import dat.entities.AudioFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalysisResultDAOTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    private EntityManagerFactory emf;
    private AnalysisResultDAO dao;

    @BeforeAll
    void setupEMF() {
        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("CONNECTION_STR", postgres.getJdbcUrl().replace("test_db", ""));
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());

        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = new AnalysisResultDAO();
    }

    @BeforeEach
    void setUp() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AudioFile audioFile = new AudioFile();
        AudioFile audioFile2 = new AudioFile();
        audioFile.setFilename("test-audio.wav");
        audioFile.setSizeBytes(1024L);
        em.persist(audioFile);
        em.persist(audioFile2);

        AnalysisResult result1 = new AnalysisResult(null, audioFile, "BPM: 120", LocalDateTime.now().minusDays(1));
        AnalysisResult result2 = new AnalysisResult(null, audioFile2, "Pitch: A", LocalDateTime.now());

        em.persist(result1);
        em.persist(result2);

        em.getTransaction().commit();
        em.close();
    }

    @AfterEach
    void tearDown() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM AnalysisResult").executeUpdate();
        em.createQuery("DELETE FROM AudioFile").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    void testFindByAudioFileId() {
        Long audioFileId = getFirstAudioFileId();
        AnalysisResult result = dao.findByAudioFileId(audioFileId);

        assertThat(result, is(notNullValue()));
        assertThat(result.getAudioFile().getId(), is(audioFileId));
    }

    @Test
    void testFindLatestByAudioFile() {
        Long audioFileId = getFirstAudioFileId();
        List<AnalysisResult> results = dao.findLatestByAudioFile(audioFileId, 1);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getResultData(), containsString("BPM"));
    }

    @Test
    void testFindByAudioFileIdAndTimeRange() {
        Long audioFileId = getFirstAudioFileId();
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusHours(12);

        List<AnalysisResult> results = dao.findByAudioFileIdAndTimeRange(audioFileId, start, end, 10);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getResultData(), containsString("BPM"));
    }

    private Long getFirstAudioFileId() {
        EntityManager em = emf.createEntityManager();
        Long id = em.createQuery("SELECT a.id FROM AudioFile a", Long.class)
                .setMaxResults(1)
                .getSingleResult();
        em.close();
        return id;
    }
}