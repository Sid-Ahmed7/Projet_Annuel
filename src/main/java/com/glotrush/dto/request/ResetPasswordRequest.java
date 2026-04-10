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
public class ResetPasswordRequest {
    @NotBlank(message = "{error.token.required}")
    private String token;

    @NotBlank(message = "{error.newPassword.required}")
    @Size(min = 12, message = "{error.newPassword.minLength}")
    private String newPassword;
}
