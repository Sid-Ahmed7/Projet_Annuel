package com.glotrush.services.images;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.glotrush.config.FileStorageConfig;
import com.glotrush.entities.UserProfile;
import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService implements IProfileService {
    
    private static final Long MAX_FILE_SIZE = 2 * 1024 * 1024L; 
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    private final FileStorageConfig fileStorageConfig;
    private final MessageSource messageSource;
    private final UserProfileRepository userProfileRepository;
    

    @Override
    @Transactional
    public String uploadImage(UUID accountId, MultipartFile file) throws IOException {
        checkSizeAndExtension(file);

        UserProfile userProfile = userProfileRepository.findByAccount_Id(accountId).orElseThrow(() -> new UserNotFoundException(
            messageSource.getMessage("error.profile.not_found", null, LocaleUtils.getCurrentLocale())
        ));

        String newFileName = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        Path uploadDirectoryPath = fileStorageConfig.getDirectoryUploadPath();
        Path targetLocation = uploadDirectoryPath.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        deleteExistingImage(userProfile);

        userProfile.setPhotoUrl(newFileName);
        userProfileRepository.save(userProfile);
        return newFileName;

    }

    @Override
    public Path getImagePath(String filename) {
        Path pathDirectory = fileStorageConfig.getDirectoryUploadPath().normalize();
        Path filePath = pathDirectory.resolve(filename).normalize();
        if(!filePath.startsWith(pathDirectory)) {
            throw new InvalidPathException(messageSource.getMessage("error.profile.path", null, LocaleUtils.getCurrentLocale()));
        }
        return filePath;

    }

    @Override
    @Transactional
    public void deleteImage(UUID accountId) {
             UserProfile userProfile = userProfileRepository.findByAccount_Id(accountId).orElseThrow(() -> new UserNotFoundException(
            messageSource.getMessage("error.profile.not_found", null, LocaleUtils.getCurrentLocale())
        ));
        deleteExistingImage(userProfile);
        userProfile.setPhotoUrl(null);
        userProfileRepository.save(userProfile);
    }



    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    private void checkSizeAndExtension(MultipartFile file) {
        if(file.isEmpty()) {
            throw new InvalidUploadException(messageSource.getMessage("image.empty", null, LocaleUtils.getCurrentLocale()));
        }

        if(file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidUploadException(messageSource.getMessage("image.tooLarge", null, LocaleUtils.getCurrentLocale()));
        }
        
        String fileExtension = getExtension(file.getOriginalFilename());
        if(!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new InvalidUploadException(messageSource.getMessage("image.invalidType", null, LocaleUtils.getCurrentLocale()));
        }
    }

    private void deleteExistingImage(UserProfile profile) {
        if(profile.getPhotoUrl() != null) {
            String existingFileName = profile.getPhotoUrl();
            Path existingFilePath = fileStorageConfig.getDirectoryUploadPath().resolve(existingFileName);
            try {
                Files.deleteIfExists(existingFilePath);
            } catch (IOException e) {
                log.error("Failed to delete existing image: {}", e.getMessage());
            }
        
        } 

    }
}
