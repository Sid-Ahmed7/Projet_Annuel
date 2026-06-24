package com.glotrush.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AIQuotaResponse {
    private int maxQuota;
    private long currentUsage;
    private long remainingQuota;
    private LocalDateTime periodEnd;
}
