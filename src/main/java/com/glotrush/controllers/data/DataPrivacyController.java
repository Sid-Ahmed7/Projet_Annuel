package com.glotrush.controllers.data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.glotrush.dto.request.DeleteAccountRequest;
import com.glotrush.services.dataPrivacy.IDataPrivacyService;
import com.glotrush.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-privacy")
@RequiredArgsConstructor
public class DataPrivacyController {

    private final IDataPrivacyService dataPrivacyService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(Authentication authentication) throws Exception {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        byte[] dataJsonFile = dataPrivacyService.exportUserData(accountId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data-" + LocalDate.now() + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dataJsonFile);
    }

    @PostMapping("/generate-code")
    public ResponseEntity<Void> generateCode(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        dataPrivacyService.generateAccountDeletionCode(accountId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest request, Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        dataPrivacyService.deleteAccount(accountId, request.getCode());
        return ResponseEntity.noContent().build();
    }
}
