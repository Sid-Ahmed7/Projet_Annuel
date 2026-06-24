package com.glotrush.services.images;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface ILessonMediaService {

    String uploadLessonImage(MultipartFile file) throws IOException;

    String uploadLessonAudio(MultipartFile file) throws IOException;

    Path getLessonImagePath(String filename);

    Path getLessonAudioPath(String filename);

    void deleteLessonImage(String filename);

    void deleteLessonAudio(String filename);
}
