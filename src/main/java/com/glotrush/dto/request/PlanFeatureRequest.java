package com.glotrush.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlanFeatureRequest {
    
    @NotBlank
    @Size(max = 255)
    private String label;

    private Integer orderIndex;
}
