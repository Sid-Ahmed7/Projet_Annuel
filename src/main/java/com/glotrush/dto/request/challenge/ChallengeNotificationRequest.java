package com.glotrush.dto.request.challenge;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.LessonType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeNotificationRequest {
    private UUID challengeId;
    private String username;
    private String photourl;
    private String lessonTitle;
    private LessonType lessonType;
    private LocalDateTime expiresAt;
}
