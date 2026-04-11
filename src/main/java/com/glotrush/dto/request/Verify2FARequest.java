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
public class Verify2FARequest {
    @NotBlank(message = "{error.tempUserId.required}")
    private String tempUserId;

    @NotBlank(message = "{error.2faCode.required}")
    @Size(min = 6, max = 6, message = "{error.2faCode.length}")
    private String code;
}
