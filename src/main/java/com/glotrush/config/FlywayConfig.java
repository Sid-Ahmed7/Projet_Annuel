package com.glotrush.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
        };
    }

    @Bean
    CommandLineRunner runFlyway(Flyway flyway) {
        return args -> {
            flyway.migrate();
        };
    }
}
