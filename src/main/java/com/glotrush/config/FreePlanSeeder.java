package com.glotrush.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.glotrush.entities.Plan;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FreePlanSeeder implements CommandLineRunner {

    private final PlanRepository planRepository;
    private final MessageSource messageSource;

    @Override
    public void run(String ... args) {
        seedFreePlan();
    }

    private void seedFreePlan() {
        boolean freePlanExists = planRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO).isPresent();
        if (!freePlanExists) {
            Plan freePlan = Plan.builder()
                .name(messageSource.getMessage("plan.free.name", null, LocaleUtils.getCurrentLocale()))
                .description(messageSource.getMessage("plan.free.description", null, LocaleUtils.getCurrentLocale()))
                .price(BigDecimal.ZERO)
                .paymentInterval(null)
                .isActive(true)
                .build();
            
            planRepository.save(freePlan);
        }
    }
}