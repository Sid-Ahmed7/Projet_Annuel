package com.glotrush.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcmAnswerRequest {
    private UUID id;
    private Integer selectedOptionIndex;
}
