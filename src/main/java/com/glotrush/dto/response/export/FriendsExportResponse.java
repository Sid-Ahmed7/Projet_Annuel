package com.glotrush.dto.response.export;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendsExportResponse {
    private UUID friendId;   
    private LocalDateTime since;
}
