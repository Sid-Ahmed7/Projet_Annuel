package com.glotrush.services.admin;

import java.util.UUID;

import com.glotrush.dto.response.AdminUserListResponse;
import com.glotrush.enumerations.AccountStatus;

public interface IAdminUserService {
    AdminUserListResponse getUsers(String search, Integer page, Integer size);
    void updateUserStatus(UUID accountId, AccountStatus status);
    void unlockUser(UUID accountId);
    void deleteUser(UUID accountId);
    void resetUserPassword(UUID accountId, String newPassword);
}
