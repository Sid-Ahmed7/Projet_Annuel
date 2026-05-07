package com.glotrush.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {};
    }

    @Bean
    ApplicationRunner flywayRunner(Flyway flyway) {
        return args -> {
            flyway.repair();
            flyway.migrate();
        };
    }

}
