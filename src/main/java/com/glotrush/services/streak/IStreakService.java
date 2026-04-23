package com.glotrush.services.streak;

import java.util.UUID;

public interface IStreakService {
    
    void updateStreakForUser(UUID accountId);
}
