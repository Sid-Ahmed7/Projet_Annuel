package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.PlanFeature;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, UUID> {
    void deleteByPlan_Id(UUID planId);

}
