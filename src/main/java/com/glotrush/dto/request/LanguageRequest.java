package com.glotrush.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LanguageRequest {
    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotNull
    private Integer orderIndex = 0;

    @NotNull
    private Boolean isActive = true;
}
