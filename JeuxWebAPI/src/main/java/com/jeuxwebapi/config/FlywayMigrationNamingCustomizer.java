package com.jeuxwebapi.config;

import io.quarkus.flyway.FlywayConfigurationCustomizer;
import jakarta.inject.Singleton;
import org.flywaydb.core.api.configuration.FluentConfiguration;

@Singleton
public class FlywayMigrationNamingCustomizer implements FlywayConfigurationCustomizer {
    @Override
    public void customize(FluentConfiguration configuration) {
        configuration.sqlMigrationSeparator("-");
    }
}
