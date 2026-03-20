package com.glotrush.services.images;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {

    String uploadImage(UUID accountId, MultipartFile file) throws IOException;
    void deleteImage(UUID accountId);
    Path getImagePath(String filename);

}
