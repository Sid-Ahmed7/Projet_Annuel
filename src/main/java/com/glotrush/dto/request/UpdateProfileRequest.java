package com.glotrush.dto.request;

import com.glotrush.enumerations.CountryCode;
import com.glotrush.enumerations.TimeZone;

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

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    private CountryCode countryCode;

    private TimeZone timezone;

    private Boolean isPublic;
}