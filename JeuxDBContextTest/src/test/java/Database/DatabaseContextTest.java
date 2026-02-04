package Database;

import Entities.League;
import Entities.Match;
import Entities.Stage;
import Entities.Team;
import Entities.Tournament;
import Services.MatchUploadService;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseContextTest {
    @Test
    void CreateDatabaseScheme() {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        Mutiny.SessionFactory sessionFactory = buildSessionFactory(dbUrl, dbUser, dbPassword, "create");
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

        Mutiny.SessionFactory sessionFactory = buildSessionFactory(dbUrl, dbUser, dbPassword, "validate");
        try {
            League league = sessionFactory.withTransaction((session, tx) -> {
                MatchUploadService service = new MatchUploadService(session);
                return service.UpsertLeague(leagueName);
            }).await().atMost(Duration.ofSeconds(10));
            Assertions.assertNotNull(league, "UpsertLeague should return a League");
        } finally {
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

        Mutiny.SessionFactory sessionFactory = buildSessionFactory(dbUrl, dbUser, dbPassword, "validate");
        try {
            Tournament upserted = sessionFactory.withTransaction((session, tx) -> {
                MatchUploadService service = new MatchUploadService(session);
                return service.UpgradeTournament(tournament);
            }).await().atMost(Duration.ofSeconds(20));
            Assertions.assertNotNull(upserted, "UpsertTournament should return a Tournament");
        } finally {
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
        Mutiny.SessionFactory sessionFactory = buildSessionFactory(dbUrl, dbUser, dbPassword, "validate");
        try {
            Boolean result = sessionFactory.withSession(session -> {
                MatchUploadService service = new MatchUploadService(session);
                return service.UploadTournament(1L, "Чемпионат России", fullPath);
            }).await().atMost(Duration.ofMinutes(2));
            Assertions.assertTrue(result, "UploadTournament should succeed");
        } finally {
            sessionFactory.close();
        }
    }

    @Test
    void UploadAllTournamentData() {
        Properties properties = loadDatabaseProperties();

        String dbUrl = properties.getProperty("db.url");
        String dbUser = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");
        validateConnectionParams(dbUrl, dbUser, dbPassword);

        String dataRoot = buildDataRoot(properties);
        Mutiny.SessionFactory sessionFactory = buildSessionFactory(dbUrl, dbUser, dbPassword, "validate");
        try {
            int processed = 0;
            try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataRoot), "*.json")) {
                for (Path file : stream) {
                    Boolean result = sessionFactory.withSession(session -> {
                        MatchUploadService service = new MatchUploadService(session);
                        return service.UploadTournament(1L, "Чемпионат России", file.toString());
                    }).await().atMost(Duration.ofMinutes(2));
                    Assertions.assertTrue(result, "UploadTournament should succeed for " + file.getFileName());
                    processed++;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to enumerate json files in " + dataRoot, exception);
            }

            Assertions.assertTrue(processed > 0, "No json files found in " + dataRoot);
        } finally {
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

    private Mutiny.SessionFactory buildSessionFactory(String dbUrl, String dbUser, String dbPassword, String hbm2ddlAuto) {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(League.class)
                .addAnnotatedClass(Tournament.class)
                .addAnnotatedClass(Stage.class)
                .addAnnotatedClass(Team.class)
                .addAnnotatedClass(Match.class);

        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.connection.url", normalizeReactiveUrl(dbUrl));
        settings.put("hibernate.connection.username", dbUser);
        settings.put("hibernate.connection.password", dbPassword);
        settings.put("hibernate.default_schema", "football");
        settings.put("hibernate.globally_quoted_identifiers", "true");
        settings.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);

        ServiceRegistry registry = new ReactiveServiceRegistryBuilder()
                .applySettings(settings)
                .build();
        return configuration.buildSessionFactory(registry)
                .unwrap(Mutiny.SessionFactory.class);
    }

    private String normalizeReactiveUrl(String dbUrl) {
        if (dbUrl == null) {
            return null;
        }
        if (dbUrl.startsWith("jdbc:")) {
            return dbUrl.substring("jdbc:".length());
        }
        return dbUrl;
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
