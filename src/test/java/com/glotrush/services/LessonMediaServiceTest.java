package com.glotrush.services;

import com.glotrush.config.FileStorageConfig;
import com.glotrush.exceptions.InvalidPathException;
import com.glotrush.exceptions.InvalidUploadException;
import com.glotrush.services.images.LessonMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonMediaService Unit Tests")
class LessonMediaServiceTest {

    @Mock
    private FileStorageConfig fileStorageConfig;

    @Mock
    private MessageSource messageSource;

    private LessonMediaService lessonMediaService;

    @TempDir
    Path temporaryFolder;

    @BeforeEach
    void setUp() {
        lessonMediaService = new LessonMediaService(fileStorageConfig, messageSource);
    }

    @Test
    @DisplayName("Should successfully upload image")
    void shouldUploadImage() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "test content".getBytes());
        when(fileStorageConfig.getLessonImagesPath()).thenReturn(temporaryFolder);

        String uploadedFilename = lessonMediaService.uploadLessonImage(file);

        assertThat(uploadedFilename).isNotNull().endsWith(".png");
        assertThat(temporaryFolder.resolve(uploadedFilename)).exists();
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
    @DisplayName("Should return valid image path")
    void shouldReturnValidImagePath() {
        when(fileStorageConfig.getLessonImagesPath()).thenReturn(temporaryFolder);

        Path path = lessonMediaService.getLessonImagePath("test.png");

        assertThat(path).isEqualTo(temporaryFolder.resolve("test.png"));
    }

    @Test
    @DisplayName("Should throw exception for path traversal filename")
    void shouldThrowExceptionForPathTraversal() {
        when(fileStorageConfig.getLessonImagesPath()).thenReturn(temporaryFolder);
        when(messageSource.getMessage(eq("error.profile.path"), any(), any(Locale.class))).thenReturn("Invalid path");

        assertThatThrownBy(() -> lessonMediaService.getLessonImagePath("../traversal.png"))
                .isInstanceOf(InvalidPathException.class)
                .hasMessage("Invalid path");
    }
}
