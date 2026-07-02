package com.glotrush.services.images;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ILessonMediaService {

    String uploadLessonImage(MultipartFile file) throws IOException;

    String uploadLessonAudio(MultipartFile file) throws IOException;

    String getLessonImageKey(String filename);

    String getLessonAudioKey(String filename);

    void deleteLessonImage(String filename);

    void deleteLessonAudio(String filename);
}