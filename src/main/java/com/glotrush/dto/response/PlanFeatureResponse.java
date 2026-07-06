package com.glotrush.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanFeatureResponse {
    private UUID id;
    private String label;
    private Integer orderIndex;
}
