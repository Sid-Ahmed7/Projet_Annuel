package com.glotrush.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FASetupRequest {
    @NotNull(message = "{error.twoFactorAuthId.required}")
    private UUID twoFactorAuthId;

    @NotBlank(message = "{error.verificationCode.required}")
    @Size(min = 6, max = 6, message = "{error.verificationCode.length}")
    private String code;
}
