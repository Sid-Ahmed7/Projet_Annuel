package com.glotrush.services.plan;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.PlanBuilder;
import com.glotrush.dto.request.CreatePlanRequest;
import com.glotrush.dto.request.UpdatePlanRequest;
import com.glotrush.dto.response.PlanResponse;
import com.glotrush.entities.Plan;
import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.exceptions.PlanNotFoundException;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService implements IPlanService {

    private final PlanRepository planRepository;
    private final PlanBuilder planBuilder;
    private final MessageSource messageSource;

    @Override
    public PlanResponse createPlan(CreatePlanRequest request) {
            Plan plan = Plan.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
            .paymentInterval(request.getPaymentInterval())
            .subscriptionType(request.getSubscriptionType())
            .stripePriceId(request.getStripePriceId())
            .isActive(true)
            .build();
        return planBuilder.mapToResponse(planRepository.save(plan));
    }

    @Override
    public PlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        Plan plan = getPlanById(planId);

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getPaymentInterval() != null) {
            plan.setPaymentInterval(request.getPaymentInterval());
        }
        if (request.getSubscriptionType() != null) {
            plan.setSubscriptionType(request.getSubscriptionType());
        }
        if (request.getStripePriceId() != null) {
            plan.setStripePriceId(request.getStripePriceId());
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }
        return planBuilder.mapToResponse(planRepository.save(plan));
    }


    @Override
    public void deletePlan(UUID planId) {
        Plan plan = getPlanById(planId);
        plan.setIsActive(false);
        planRepository.save(plan);
    }

    @Override
    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findAllByIsActiveTrueOrderByPriceAsc().stream()
                .map(planBuilder::mapToResponse)
                .toList();
    }


    @Override
    public List<PlanResponse> getPlansByPaymentInterval(PaymentInterval paymentInterval) {
        return planRepository.findAllByPaymentIntervalAndIsActiveTrue(paymentInterval).stream().
        map(planBuilder::mapToResponse).toList();
    }

    @Override
    public Plan getPlanById(UUID planId) {
        return planRepository.findById(planId)
        .orElseThrow(() -> new PlanNotFoundException(
            messageSource.getMessage("error.plan.notfound",null, LocaleUtils.getCurrentLocale())
        ));
    }

    @Override
    public PlanResponse togglePlanStatus(UUID planId) {
        Plan plan = getPlanById(planId);
        plan.setIsActive(!plan.getIsActive());
        return planBuilder.mapToResponse(planRepository.save(plan));
    }

    @Override
    public List<PlanResponse> getAllPlansForAdmin() {
        return planRepository.findAllByOrderByPriceAsc().stream()
                .map(planBuilder::mapToResponse)
                .toList();
    }
   
}
