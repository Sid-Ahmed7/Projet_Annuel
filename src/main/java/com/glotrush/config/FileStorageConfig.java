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
}
