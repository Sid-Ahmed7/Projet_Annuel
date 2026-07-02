package com.glotrush.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Profile("dev | test")
public class LocalFileStorageService implements FileStorageService {

    private final Path rootPath;

    public LocalFileStorageService(@Value("${upload-dir}") String uploadDir) {
        this.rootPath = Paths.get(uploadDir).normalize().toAbsolutePath();
    }

    @Override
    public void upload(MultipartFile file, String key) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream download(String key) throws IOException {
        Path filePath = resolve(key);
        if (!Files.exists(filePath)) {
            throw new StorageFileNotFoundException(key);
        }
        return Files.newInputStream(filePath);
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {}
    }

    private Path resolve(String key) {
        Path resolved = rootPath.resolve(key).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new StorageFileNotFoundException(key);
        }
        return resolved;
    }
}
