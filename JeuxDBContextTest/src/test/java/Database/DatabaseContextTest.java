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
                .setProperty("hibernate.hbm2ddl.auto", "create");

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        sessionFactory.close();
    }

    private Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("database.properties not found in test resources");
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load database.properties", exception);
        }
        return properties;
    }

    private void validateConnectionParams(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("db.url is missing or blank in database.properties");
        }
        if (dbUser == null || dbUser.isBlank()) {
            throw new IllegalStateException("db.user is missing or blank in database.properties");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("db.password is missing or blank in database.properties");
        }
    }
}
