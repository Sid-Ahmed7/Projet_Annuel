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
import com.glotrush.constants.LessonMediaConstants;
import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class LessonMediaService implements ILessonMediaService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(LessonMediaConstants.IMAGE_EXTENSIONS.split(","));
    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(LessonMediaConstants.AUDIO_EXTENSIONS.split(","));

    private final FileStorageConfig fileStorageConfig;
    private final MessageSource messageSource;

    @Override
    public String uploadLessonImage(MultipartFile file) throws IOException {
        checkFileValidity(file, LessonMediaConstants.MAX_IMAGE_FILE_SIZE, ALLOWED_IMAGE_EXTENSIONS);

        String newFileName = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        Path uploadDirectoryPath = fileStorageConfig.getLessonImagesPath();
        Path targetLocation = uploadDirectoryPath.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }

    @Override
    public String uploadLessonAudio(MultipartFile file) throws IOException {
        checkFileValidity(file, LessonMediaConstants.MAX_AUDIO_FILE_SIZE, ALLOWED_AUDIO_EXTENSIONS);

        String newFileName = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        Path uploadDirectoryPath = fileStorageConfig.getLessonAudiosPath();
        Path targetLocation = uploadDirectoryPath.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }

    @Override
    public Path getLessonImagePath(String filename) {
        Path pathDirectory = fileStorageConfig.getLessonImagesPath().normalize();
        Path filePath = pathDirectory.resolve(filename).normalize();
        if (!filePath.startsWith(pathDirectory)) {
            throw new InvalidPathException(messageSource.getMessage("error.profile.path", null, LocaleUtils.getCurrentLocale()));
        }
        return filePath;
    }

    @Override
    public Path getLessonAudioPath(String filename) {
        Path pathDirectory = fileStorageConfig.getLessonAudiosPath().normalize();
        Path filePath = pathDirectory.resolve(filename).normalize();
        if (!filePath.startsWith(pathDirectory)) {
            throw new InvalidPathException(messageSource.getMessage("error.profile.path", null, LocaleUtils.getCurrentLocale()));
        }
        return filePath;
    }

    @Override
    public void deleteLessonImage(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path filePath = getLessonImagePath(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete lesson image file {}: {}", filename, e.getMessage());
        }
    }

    @Override
    public void deleteLessonAudio(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path filePath = getLessonAudioPath(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete lesson audio file {}: {}", filename, e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void checkFileValidity(MultipartFile file, Long maxFileSize, List<String> allowedExtensions) {
        if (file.isEmpty()) {
            throw new InvalidUploadException(messageSource.getMessage("image.empty", null, LocaleUtils.getCurrentLocale()));
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidUploadException(messageSource.getMessage("image.tooLarge", null, LocaleUtils.getCurrentLocale()));
        }

        String fileExtension = getExtension(file.getOriginalFilename());
        if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new InvalidUploadException(messageSource.getMessage("image.invalidType", null, LocaleUtils.getCurrentLocale()));
        }
    }
}
