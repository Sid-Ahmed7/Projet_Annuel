package com.glotrush.dto.request;

import jakarta.validation.constraints.Email;
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
public class ContactRequest {

    @NotBlank(message = "{error.name.notBlank}")
    @Size(max = 100, message = "{error.name.maxLength}")
    private String name;

    @NotBlank(message = "{error.email.notBlank}")
    @Email
    private String email;

    @NotBlank(message = "{error.subject.notBlank}")
    @Size(max = 50, message = "{error.subject.maxLength}")
    private String subject;

    @NotBlank(message = "{error.message.notBlank}")
    private String message;
    
}
