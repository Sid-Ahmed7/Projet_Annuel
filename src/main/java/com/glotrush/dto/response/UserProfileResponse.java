package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
public class UserProfileResponse {
    
    private UUID id;
    private UUID accountId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String bio;
    private String displayName;
    private CountryCode countryCode;
    private TimeZone timezone;
    private Boolean isPublic;
    private List<UserLanguageResponse> languages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
