package com.glotrush.controllers.admin;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.AdminResetPasswordRequest;
import com.glotrush.dto.request.UpdateUserStatusRequest;
import com.glotrush.dto.response.AdminUserListResponse;
import com.glotrush.services.admin.IAdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IAdminUserService adminUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<AdminUserListResponse> getUsers(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(adminUserService.getUsers(search, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable UUID userId, @Valid @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID userId) {
        adminUserService.unlockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable UUID userId, @Valid @RequestBody AdminResetPasswordRequest request) {
        adminUserService.resetUserPassword(userId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
