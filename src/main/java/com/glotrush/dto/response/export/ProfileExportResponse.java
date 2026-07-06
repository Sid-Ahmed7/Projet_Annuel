package com.glotrush.dto.response.export;

import com.glotrush.enumerations.CountryCode;
import com.glotrush.enumerations.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileExportResponse {
    private String bio;
    private CountryCode countryCode;
    private TimeZone timezone;
    private Boolean isPublic;
    private Boolean hasCompletedOnboarding; 
}
