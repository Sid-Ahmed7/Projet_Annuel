package com.glotrush.constants;

public final class LessonMediaConstants {

    private LessonMediaConstants() {}

    public static final Long MAX_IMAGE_FILE_SIZE = 2 * 1024 * 1024L;
    public static final Long MAX_AUDIO_FILE_SIZE = 5 * 1024 * 1024L;
    public static final String IMAGE_EXTENSIONS = "jpg,jpeg,png,webp";
    public static final String AUDIO_EXTENSIONS = "mp3,wav,ogg,m4a";
    public static final String IMAGE_BASE_URL = "/api/v1/lessons/media/images/";
    public static final String AUDIO_BASE_URL = "/api/v1/lessons/media/audios/";
}
