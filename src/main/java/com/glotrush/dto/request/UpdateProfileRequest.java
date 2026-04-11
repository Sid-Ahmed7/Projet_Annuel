package com.glotrush.dto.request;

import com.glotrush.enumerations.CountryCode;
import com.glotrush.enumerations.TimeZone;

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
public class UpdateProfileRequest {

    @Size(max = 500, message = "{error.photoUrl.maxLength}")
    private String photoUrl;

    @Size(max = 500, message = "{error.bio.maxLength}")
    private String bio;

    @Size(min = 3, max = 50, message = "{error.username.length}")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "{error.username.pattern}")
    private String username;

    private CountryCode countryCode;

    private TimeZone timezone;

    private Boolean isPublic;
}