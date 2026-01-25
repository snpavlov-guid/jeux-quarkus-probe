package Database;

import Entities.League;
import Entities.Match;
import Entities.Stage;
import Entities.Team;
import Entities.Tournament;
import Services.MatchUploadService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseContextTest {
    @Test
    void CreateDatabaseScheme() {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        Configuration configuration = buildConfiguration(dbUrl, dbUser, dbPassword, "create");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        sessionFactory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"РПЛ"})
    void UpsertLeagueTest(String leagueName) {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        Configuration configuration = buildConfiguration(dbUrl, dbUser, dbPassword, "validate");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        try {
            MatchUploadService service = new MatchUploadService(session);
            League league = service.UpsertLeague(leagueName);
            Assertions.assertNotNull(league, "UpsertLeague should return a League");
            session.getTransaction().commit();
        } catch (RuntimeException exception) {
            session.getTransaction().rollback();
            throw exception;
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Champ_1992.json", "Champ_2013-2014.json"})
    void UpsertTournamentTest(String turnirFile) {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        String dataRoot = buildDataRoot(properties);
        String fullPath = Paths.get(dataRoot, turnirFile).toString();

        Tournament tournament = buildTournament(turnirFile);
        Assertions.assertNotNull(tournament.getLeague(), "League must be set");
        Assertions.assertNotNull(tournament.getName(), "Tournament name must be set");
        Assertions.assertNotNull(fullPath, "Full path must be built");

        Configuration configuration = buildConfiguration(dbUrl, dbUser, dbPassword, "validate");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        try {
            MatchUploadService service = new MatchUploadService(session);
            Tournament upserted = service.UpgradeTournament(tournament);
            Assertions.assertNotNull(upserted, "UpsertTournament should return a Tournament");
            session.getTransaction().commit();
        } catch (RuntimeException exception) {
            session.getTransaction().rollback();
            throw exception;
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Champ_1992.json"})
    void UploadTournamentTest(String turnirDataFile) {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        String dataRoot = buildDataRoot(properties);
        String fullPath = Paths.get(dataRoot, turnirDataFile).toString();
        Configuration configuration = buildConfiguration(dbUrl, dbUser, dbPassword, "validate");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        try {
            MatchUploadService service = new MatchUploadService(session);
            boolean result = service.UploadTournament(1L, "Чемпионат России", fullPath);
            Assertions.assertTrue(result, "UploadTournament should succeed");
        } catch (RuntimeException exception) {
            throw exception;
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    private Properties loadDatabaseProperties() {
        Path configPath = Paths.get("config.properties");
        if (!Files.exists(configPath)) {
            configPath = Paths.get("JeuxDBContextTest", "config.properties");
        }
        if (!Files.exists(configPath)) {
            throw new IllegalStateException("config.properties not found in module root");
        }
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load config.properties", exception);
        }
        return properties;
    }

    private String buildDataRoot(Properties properties) {
        String dataRoot = properties.getProperty("data.soccer365");
        if (dataRoot == null || dataRoot.isBlank()) {
            throw new IllegalStateException("data.soccer365 is missing or blank in config.properties");
        }
        String trimmed = dataRoot.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private Tournament buildTournament(String turnirFile) {
        String fileName = turnirFile;
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.length() - ".json".length());
        }
        String seasonPart = fileName.replaceFirst("^Champ_", "");
        Integer stYear = null;
        Integer fnYear = null;
        if (seasonPart.contains("-")) {
            String[] parts = seasonPart.split("-", 2);
            stYear = Integer.parseInt(parts[0]);
            fnYear = Integer.parseInt(parts[1]);
        } else {
            stYear = Integer.parseInt(seasonPart);
        }

        League league = new League();
        league.setId(1);

        Tournament tournament = new Tournament();
        tournament.setLeague(league);
        tournament.setName("Чемпионат России");
        tournament.setStYear(stYear);
        tournament.setFnYear(fnYear);
        return tournament;
    }

    private Configuration buildConfiguration(String dbUrl, String dbUser, String dbPassword, String hbm2ddlAuto) {
        return new Configuration()
                .addAnnotatedClass(League.class)
                .addAnnotatedClass(Tournament.class)
                .addAnnotatedClass(Stage.class)
                .addAnnotatedClass(Team.class)
                .addAnnotatedClass(Match.class)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", dbUrl)
                .setProperty("hibernate.connection.username", dbUser)
                .setProperty("hibernate.connection.password", dbPassword)
                .setProperty("hibernate.default_schema", "football")
                .setProperty("hibernate.globally_quoted_identifiers", "true")
                .setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
    }

    private void validateConnectionParams(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("db.url is missing or blank in config.properties");
        }
        if (dbUser == null || dbUser.isBlank()) {
            throw new IllegalStateException("db.user is missing or blank in config.properties");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("db.password is missing or blank in config.properties");
        }
    }
}
