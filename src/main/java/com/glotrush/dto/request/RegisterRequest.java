package com.glotrush.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{error.email.required}")
    @Email(message = "{error.email.invalid}")
    private String email;

    @NotBlank(message = "{error.password.required}")
    @Size(min = 12, message = "{error.password.minLength}")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{12,}$",
            message = "{error.password.strong}")
    private String password;

    @NotBlank(message = "{error.firstName.required}")
    private String firstName;
    
    @NotBlank(message = "{error.lastName.required}")
    private String lastName;

    @Size(min = 3, max = 50, message = "{error.username.length}")
    private String username;
}
