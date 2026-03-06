package com.glotrush.config;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.glotrush.entities.Plan;
import com.glotrush.repositories.PlanRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FreePlanSeeder implements CommandLineRunner {

    private final PlanRepository planRepository;
    private final MessageSource messageSource;

  protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Override
    public void run(String ... args) {
        seedFreePlan();
    }

    private void seedFreePlan() {
        boolean freePlanExists = planRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO).isPresent();
        if (!freePlanExists) {
            Plan freePlan = Plan.builder()
                .name(messageSource.getMessage("plan.free.name", null, getCurrentLocale()))
                .description(messageSource.getMessage("plan.free.description", null, getCurrentLocale()))
                .price(BigDecimal.ZERO)
                .paymentInterval(null)
                .isActive(true)
                .build();
            
            planRepository.save(freePlan);
        }
    }
}