package com.glotrush.services.dataPrivacy;

import java.util.UUID;

public interface IDataPrivacyService {

    byte[] exportUserData(UUID accountId) throws Exception;
    void deleteAccount(UUID accountId, String confirmPassword);
}
