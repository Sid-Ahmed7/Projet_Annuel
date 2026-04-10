package com.glotrush.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disable2FARequest {
    @NotBlank(message = "{error.code.required}")
    @Size(min = 6, max = 6, message = "{error.code.invalid}")
    private String code;
}
