package com.glotrush.services.images;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {

    String uploadImage(UUID accountId, MultipartFile file) throws IOException;

    void deleteImage(UUID accountId);

    String getImageKey(String filename);
}