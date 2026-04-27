package com.glotrush.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * Configuration de test pour le MessageSource.
 * Cette classe permet d'initialiser un MessageSource fonctionnel dans les tests unitaires
 * afin d'éviter les valeurs null lors de la résolution des messages i18n.
 */
@Configuration
public class TestMessageSourceConfig {

    @Bean
    @Primary
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.ENGLISH); 
        messageSource.setFallbackToSystemLocale(false); 
        messageSource.setUseCodeAsDefaultMessage(true); 
        return messageSource;
    }
}
