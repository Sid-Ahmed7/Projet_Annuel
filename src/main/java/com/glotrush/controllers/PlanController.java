package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.builder.PlanBuilder;
import com.glotrush.dto.request.CreatePlanRequest;
import com.glotrush.dto.request.UpdatePlanRequest;
import com.glotrush.dto.response.PlanResponse;
import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.services.plan.IPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping ("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final IPlanService planService;
    private final PlanBuilder planBuilder;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<PlanResponse> createAPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(@PathVariable UUID planId, @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(planId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID planId) {
        planService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }
    

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/interval/{interval}")
    public ResponseEntity<List<PlanResponse>> getPlansByInterval(@PathVariable PaymentInterval interval) {
        return ResponseEntity.ok(planService.getPlansByPaymentInterval(interval));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable UUID planId) {
        return ResponseEntity.ok(planBuilder.mapToResponse(planService.getPlanById(planId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{planId}/toggle-status")
    public ResponseEntity<PlanResponse> togglePlanStatus(@PathVariable UUID planId) {
        return ResponseEntity.ok(planService.togglePlanStatus(planId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<PlanResponse>> getAllPlansForAdmin() {
        return ResponseEntity.ok(planService.getAllPlansForAdmin());
    }

}

