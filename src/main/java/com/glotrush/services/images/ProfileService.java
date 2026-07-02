package com.glotrush.services.images;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.glotrush.constants.ImageConstants;
import com.glotrush.entities.UserProfile;
import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService implements IProfileService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(ImageConstants.EXTENSIONS.split(","));
    private static final String KEY_PREFIX = "profiles/";

    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public String uploadImage(UUID accountId, MultipartFile file) throws IOException {
        checkSizeAndExtension(file);

        UserProfile userProfile = userProfileRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.profile.not_found", null, LocaleUtils.getCurrentLocale())));

        String filename = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        fileStorageService.upload(file, KEY_PREFIX + filename);
        deleteExistingImage(userProfile);

        userProfile.setPhotoUrl(filename);
        userProfileRepository.save(userProfile);
        return filename;
    }

    @Override
    public String getImageKey(String filename) {
        if (filename.contains("..") || filename.contains("/")) {
            throw new InvalidPathException(messageSource.getMessage("error.profile.path", null, LocaleUtils.getCurrentLocale()));
        }
        return KEY_PREFIX + filename;
    }

    @Override
    @Transactional
    public void deleteImage(UUID accountId) {
        UserProfile userProfile = userProfileRepository.findByAccount_Id(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.profile.not_found", null, LocaleUtils.getCurrentLocale())));
        deleteExistingImage(userProfile);
        userProfile.setPhotoUrl(null);
        userProfileRepository.save(userProfile);
    }

    private void deleteExistingImage(UserProfile profile) {
        if (profile.getPhotoUrl() != null) {
            fileStorageService.delete(KEY_PREFIX + profile.getPhotoUrl());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void checkSizeAndExtension(MultipartFile file) {
        if (file.isEmpty())
            throw new InvalidUploadException(messageSource.getMessage("image.empty", null, LocaleUtils.getCurrentLocale()));
        if (file.getSize() > ImageConstants.MAX_FILE_SIZE)
            throw new InvalidUploadException(messageSource.getMessage("image.tooLarge", null, LocaleUtils.getCurrentLocale()));
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase()))
            throw new InvalidUploadException(messageSource.getMessage("image.invalidType", null, LocaleUtils.getCurrentLocale()));
    }
}