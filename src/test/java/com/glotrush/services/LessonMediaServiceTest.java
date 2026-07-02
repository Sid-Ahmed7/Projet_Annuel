package com.glotrush.services;

import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.services.images.LessonMediaService;
import com.glotrush.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonMediaService Unit Tests")
class LessonMediaServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MessageSource messageSource;

    private LessonMediaService lessonMediaService;

    @BeforeEach
    void setUp() {
        lessonMediaService = new LessonMediaService(fileStorageService, messageSource);
    }

    @Test
    @DisplayName("Should successfully upload image and return bare filename")
    void shouldUploadImage() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "test content".getBytes());

        String uploadedFilename = lessonMediaService.uploadLessonImage(file);

        assertThat(uploadedFilename).isNotNull().endsWith(".png").doesNotContain("/");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorageService).upload(eq(file), keyCaptor.capture());
        assertThat(keyCaptor.getValue()).startsWith("lessons/images/").endsWith(".png");
    }

    @Test
    @DisplayName("Should successfully upload audio and return bare filename")
    void shouldUploadAudio() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "test content".getBytes());

        String uploadedFilename = lessonMediaService.uploadLessonAudio(file);

        assertThat(uploadedFilename).isNotNull().endsWith(".mp3").doesNotContain("/");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorageService).upload(eq(file), keyCaptor.capture());
        assertThat(keyCaptor.getValue()).startsWith("lessons/audios/").endsWith(".mp3");
    }

    @Test
    @DisplayName("Should throw exception when uploading empty file")
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[0]);
        when(messageSource.getMessage(eq("image.empty"), any(), any(Locale.class))).thenReturn("File is empty");

        assertThatThrownBy(() -> lessonMediaService.uploadLessonImage(file))
                .isInstanceOf(InvalidUploadException.class)
                .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should throw exception when file is too large")
    void shouldThrowExceptionWhenFileIsTooLarge() {
        byte[] largeBytes = new byte[3 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", largeBytes);
        when(messageSource.getMessage(eq("image.tooLarge"), any(), any(Locale.class))).thenReturn("File too large");

        assertThatThrownBy(() -> lessonMediaService.uploadLessonImage(file))
                .isInstanceOf(InvalidUploadException.class)
                .hasMessage("File too large");
    }

    @Test
    @DisplayName("Should throw exception when extension is not allowed")
    void shouldThrowExceptionWhenExtensionNotAllowed() {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "test content".getBytes());
        when(messageSource.getMessage(eq("image.invalidType"), any(), any(Locale.class))).thenReturn("Invalid extension");

        assertThatThrownBy(() -> lessonMediaService.uploadLessonImage(file))
                .isInstanceOf(InvalidUploadException.class)
                .hasMessage("Invalid extension");
    }

    @Test
    @DisplayName("Should return image key with correct prefix")
    void shouldReturnValidImageKey() {
        String key = lessonMediaService.getLessonImageKey("test.png");

        assertThat(key).isEqualTo("lessons/images/test.png");
    }

    @Test
    @DisplayName("Should return audio key with correct prefix")
    void shouldReturnValidAudioKey() {
        String key = lessonMediaService.getLessonAudioKey("test.mp3");

        assertThat(key).isEqualTo("lessons/audios/test.mp3");
    }

    @Test
    @DisplayName("Should throw exception for path traversal in image key")
    void shouldThrowExceptionForPathTraversal() {
        when(messageSource.getMessage(eq("error.profile.path"), any(), any(Locale.class))).thenReturn("Invalid path");

        assertThatThrownBy(() -> lessonMediaService.getLessonImageKey("../traversal.png"))
                .isInstanceOf(InvalidPathException.class)
                .hasMessage("Invalid path");
    }

    @Test
    @DisplayName("Should throw exception for slash in filename")
    void shouldThrowExceptionForSlashInFilename() {
        when(messageSource.getMessage(eq("error.profile.path"), any(), any(Locale.class))).thenReturn("Invalid path");

        assertThatThrownBy(() -> lessonMediaService.getLessonImageKey("sub/image.png"))
                .isInstanceOf(InvalidPathException.class)
                .hasMessage("Invalid path");
    }
}
