package com.glotrush.services.images;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.glotrush.constants.LessonMediaConstants;
import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class LessonMediaService implements ILessonMediaService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(LessonMediaConstants.IMAGE_EXTENSIONS.split(","));
    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(LessonMediaConstants.AUDIO_EXTENSIONS.split(","));
    private static final String IMAGE_PREFIX = "lessons/images/";
    private static final String AUDIO_PREFIX = "lessons/audios/";

    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    @Override
    public String uploadLessonImage(MultipartFile file) throws IOException {
        checkFileValidity(file, LessonMediaConstants.MAX_IMAGE_FILE_SIZE, ALLOWED_IMAGE_EXTENSIONS);
        String filename = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        fileStorageService.upload(file, IMAGE_PREFIX + filename);
        return filename;
    }

    @Override
    public String uploadLessonAudio(MultipartFile file) throws IOException {
        checkFileValidity(file, LessonMediaConstants.MAX_AUDIO_FILE_SIZE, ALLOWED_AUDIO_EXTENSIONS);
        String filename = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        fileStorageService.upload(file, AUDIO_PREFIX + filename);
        return filename;
    }

    @Override
    public String getLessonImageKey(String filename) {
        return sanitizeAndPrefix(IMAGE_PREFIX, filename);
    }

    @Override
    public String getLessonAudioKey(String filename) {
        return sanitizeAndPrefix(AUDIO_PREFIX, filename);
    }

    @Override
    public void deleteLessonImage(String filename) {
        if (filename == null || filename.isBlank()) return;
        fileStorageService.delete(IMAGE_PREFIX + filename);
    }

    @Override
    public void deleteLessonAudio(String filename) {
        if (filename == null || filename.isBlank()) return;
        fileStorageService.delete(AUDIO_PREFIX + filename);
    }

    private String sanitizeAndPrefix(String prefix, String filename) {
        if (filename.contains("..") || filename.contains("/")) {
            throw new InvalidPathException(messageSource.getMessage("error.profile.path", null, LocaleUtils.getCurrentLocale()));
        }
        return prefix + filename;
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void checkFileValidity(MultipartFile file, Long maxSize, List<String> allowedExt) {
        if (file.isEmpty())
            throw new InvalidUploadException(messageSource.getMessage("image.empty", null, LocaleUtils.getCurrentLocale()));
        if (file.getSize() > maxSize)
            throw new InvalidUploadException(messageSource.getMessage("image.tooLarge", null, LocaleUtils.getCurrentLocale()));
        String extension = getExtension(file.getOriginalFilename());
        if (!allowedExt.contains(extension.toLowerCase()))
            throw new InvalidUploadException(messageSource.getMessage("image.invalidType", null, LocaleUtils.getCurrentLocale()));
    }
}