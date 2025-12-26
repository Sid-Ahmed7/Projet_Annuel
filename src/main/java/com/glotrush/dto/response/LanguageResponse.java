package com.glotrush.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageResponse {
    private UUID id;
    private String code;
    private String name;
    private String flagIcon;
    private Boolean isActive;
    private Integer orderIndex;
}
