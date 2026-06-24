package com.glotrush.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

import com.glotrush.exceptions.DirectoryCreationException;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FileStorageConfig {
    
    @Value("${upload-dir}")
    private String uploadDirectory;

    @Value("${glotrush.lesson.media.images-dir:uploads/lessons/images}")
    private String lessonImagesDirectory;

    @Value("${glotrush.lesson.media.audios-dir:uploads/lessons/audios}")
    private String lessonAudiosDirectory;

    private final MessageSource messageSource;

    public Path getDirectoryUploadPath() {
        Path path = Paths.get(uploadDirectory).normalize();
        try {
            Files.createDirectories(path);
        } catch(IOException e) {
            throw new DirectoryCreationException(messageSource.getMessage("directory.creation.failed", null, LocaleUtils.getCurrentLocale()));
        }
        return path;
    }

    public Path getLessonImagesPath() {
        Path path = Paths.get(lessonImagesDirectory).normalize();
        try {
            Files.createDirectories(path);
        } catch(IOException e) {
            throw new DirectoryCreationException(messageSource.getMessage("directory.creation.failed", null, LocaleUtils.getCurrentLocale()));
        }
        return path;
    }

    public Path getLessonAudiosPath() {
        Path path = Paths.get(lessonAudiosDirectory).normalize();
        try {
            Files.createDirectories(path);
        } catch(IOException e) {
            throw new DirectoryCreationException(messageSource.getMessage("directory.creation.failed", null, LocaleUtils.getCurrentLocale()));
        }
        return path;
    }
}
