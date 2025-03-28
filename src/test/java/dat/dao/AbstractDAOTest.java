package dat.dao;

import dat.config.HibernateConfig;
import dat.entities.AnalysisResult;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AbstractDAOTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    private static EntityManagerFactory emf;
    private static AbstractDAO<AnalysisResult> dao;
    private AnalysisResult testResult;

    @BeforeAll
    static void setupAll() {
        postgres.start();
        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("CONNECTION_STR", postgres.getJdbcUrl().replace(postgres.getDatabaseName(), ""));
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = new AbstractDAO<>(AnalysisResult.class) {};
    }

    @BeforeEach
    void setUp() {
        testResult = new AnalysisResult();
        testResult.setResultData("A minor");
        dao.save(testResult);
    }

    @AfterEach
    void cleanUp() {
        dao.delete(testResult);
    }

    @AfterAll
    static void tearDownAll() {
        postgres.stop();
    }

    @Test
    @Order(1)
    void testSaveAndFindById() {
        AnalysisResult found = dao.findById(testResult.getId());
        assertThat(found, is(notNullValue()));
        assertThat(found.getResultData(), is("A minor"));
    }

    @Test
    @Order(2)
    void testFindAll() {
        List<AnalysisResult> results = dao.findAll();
        assertThat(results, is(not(empty())));
    }

    @Test
    @Order(3)
    void testUpdate() {
        testResult.setResultData("C# Major");
        dao.update(testResult);
        AnalysisResult updated = dao.findById(testResult.getId());
        assertThat(updated.getResultData(), is("C# Major"));
    }

    @Test
    @Order(4)
    void testMerge() {
        testResult.setResultData("D minor");
        dao.merge(testResult);
        AnalysisResult merged = dao.findById(testResult.getId());
        assertThat(merged.getResultData(), is("D minor"));
    }

    @Test
    @Order(5)
    void testDelete() {
        dao.delete(testResult);
        AnalysisResult deleted = dao.findById(testResult.getId());
        assertThat(deleted, is(nullValue()));
    }
}
