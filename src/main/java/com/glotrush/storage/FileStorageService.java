package com.glotrush.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    void upload(MultipartFile file, String key) throws IOException;

    InputStream download(String key) throws IOException;

    void delete(String key);
}
