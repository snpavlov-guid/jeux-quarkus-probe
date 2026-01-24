package Database;

import Entities.League;
import Entities.Match;
import Entities.Stage;
import Entities.Team;
import Entities.Tournament;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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

        Configuration configuration = new Configuration()
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
                .setProperty("hibernate.hbm2ddl.auto", "create");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        sessionFactory.close();
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
