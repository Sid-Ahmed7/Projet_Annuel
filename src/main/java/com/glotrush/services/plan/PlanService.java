package com.glotrush.services.plan;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.glotrush.builder.PlanBuilder;
import com.glotrush.dto.request.CreatePlanRequest;
import com.glotrush.dto.request.PlanFeatureRequest;
import com.glotrush.dto.request.UpdatePlanRequest;
import com.glotrush.dto.response.PlanResponse;
import com.glotrush.entities.Plan;
import com.glotrush.entities.PlanFeature;
import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.exceptions.PlanAlreadyExistsException;
import com.glotrush.exceptions.PlanNotFoundException;
import com.glotrush.repositories.PlanFeatureRepository;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService implements IPlanService {

    private final PlanRepository planRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final PlanBuilder planBuilder;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (planRepository.existsBySubscriptionType(request.getSubscriptionType())) {
            throw new PlanAlreadyExistsException(messageSource.getMessage("error.plan.already_exists", null, LocaleUtils.getCurrentLocale()));
        }
        
        Plan plan = Plan.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
            .paymentInterval(request.getPaymentInterval())
            .subscriptionType(request.getSubscriptionType())
            .stripePriceId(request.getStripePriceId())
            .aiQuota(request.getAiQuota())
            .isActive(true)                
            .build();
        Plan savedPlan = planRepository.save(plan);
        saveFeatures(savedPlan, request.getFeatures());
        return planBuilder.mapToResponse(savedPlan);
    }

    @Override
    @Transactional
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
        if (request.getAiQuota() != null) {
            plan.setAiQuota(request.getAiQuota());
        }
        if (request.getFeatures() != null) {
            plan.getFeatures().clear();
            int index = 0;
            for (PlanFeatureRequest planFeature : request.getFeatures()) {
                plan.getFeatures().add(PlanFeature.builder()
                    .plan(plan)
                    .label(planFeature.getLabel())
                    .orderIndex(planFeature.getOrderIndex() != null ? planFeature.getOrderIndex() : index)
                    .build());
                index++;
            }
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
        return planRepository.findAllByIsActiveTrueOrderByPriceAsc().stream().map(planBuilder::mapToResponse).toList();
    }


    @Override
    public List<PlanResponse> getPlansByPaymentInterval(PaymentInterval paymentInterval) {
        return planRepository.findAllByPaymentIntervalAndIsActiveTrue(paymentInterval).stream().map(planBuilder::mapToResponse).toList();
    }

    @Override
    public Plan getPlanById(UUID planId) {
        return planRepository.findById(planId).orElseThrow(() -> new PlanNotFoundException(
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
        return planRepository.findAllByOrderByPriceAsc().stream().map(planBuilder::mapToResponse).toList();
    }

    private void saveFeatures(Plan plan, List<PlanFeatureRequest> featuresList) {
        if (featuresList == null || featuresList.isEmpty()) return;
        int index = 0;
        for (PlanFeatureRequest features : featuresList) {
            PlanFeature feature = PlanFeature.builder()
                .plan(plan)
                .label(features.getLabel())
                .orderIndex(features.getOrderIndex() != null ? features.getOrderIndex() : index)
                .build();
            planFeatureRepository.save(feature);
            index++;
        }
    }
   
}
