package com.jeuxwebapitest.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.Flyway;

public class H2FlywayTestResource implements QuarkusTestResourceLifecycleManager {
    private static final String DB_NAME = "jeuxwebapitest";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "sa";
    private static final String H2_OPTIONS = "MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=ORDER;DB_CLOSE_DELAY=-1";

    @Override
    public Map<String, String> start() {
        String jdbcUrl = "jdbc:h2:mem:" + DB_NAME + ";" + H2_OPTIONS;

        Flyway.configure()
                .dataSource(jdbcUrl, USERNAME, PASSWORD)
                .locations("classpath:db_migrations/h2")
                .sqlMigrationSeparator("-")
                .cleanDisabled(false)
                .load()
                .migrate();

        Map<String, String> props = new HashMap<>();
        props.put("quarkus.datasource.db-kind", "h2");
        props.put("quarkus.datasource.username", USERNAME);
        props.put("quarkus.datasource.password", PASSWORD);
        props.put("quarkus.datasource.reactive.url", "h2:mem:" + DB_NAME + ";" + H2_OPTIONS);
        props.put("quarkus.datasource.reactive.max-size", "16");
        return props;
    }

    @Override
    public void stop() {
        // Nothing to close explicitly for in-memory H2.
    }
}
