package com.glotrush.dto.request;

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

    @Size(min = 2, max = 2, message = "Country code must be 2 characters (ISO 3166-1)")
    private String countryCode;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    private Boolean isPublic;
}