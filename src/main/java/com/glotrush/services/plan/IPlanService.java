package com.glotrush.services.plan;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.CreatePlanRequest;
import com.glotrush.dto.request.UpdatePlanRequest;
import com.glotrush.dto.response.PlanResponse;
import com.glotrush.entities.Plan;
import com.glotrush.enumerations.PaymentInterval;

public interface IPlanService {

    PlanResponse createPlan(CreatePlanRequest plan);
    PlanResponse updatePlan(UUID planId, UpdatePlanRequest plan);
    void deletePlan(UUID planId);
    List<PlanResponse> getAllActivePlans();
    List<PlanResponse> getPlansByPaymentInterval(PaymentInterval paymentInterval);
    Plan getPlanById(UUID planId);
}
