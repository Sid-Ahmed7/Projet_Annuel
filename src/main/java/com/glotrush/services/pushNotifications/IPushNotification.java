package com.glotrush.services.pushNotifications;

import java.util.UUID;

public interface IPushNotification {

    void sendNotification(UUID accountId, String title, String message);
    
}
