package com.glotrush.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.glotrush.constants.LessonMediaConstants;
import com.glotrush.dto.response.UploadResponse;
import com.glotrush.services.images.ILessonMediaService;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lessons/media")
@RequiredArgsConstructor
public class LessonMediaController {

    private final ILessonMediaService lessonMediaService;
    private final MessageSource messageSource;

    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> uploadLessonImage(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = lessonMediaService.uploadLessonImage(file);
        String imageUrl = LessonMediaConstants.IMAGE_BASE_URL + filename;

        return ResponseEntity.ok(UploadResponse.builder()
                .message(messageSource.getMessage("success.image.uploaded", null, LocaleUtils.getCurrentLocale()))
                .pathFile(imageUrl)
                .build());
    }

    @PostMapping(value = "/audios/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> uploadLessonAudio(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = lessonMediaService.uploadLessonAudio(file);
        String audioUrl = LessonMediaConstants.AUDIO_BASE_URL + filename;

        return ResponseEntity.ok(UploadResponse.builder()
                .message(messageSource.getMessage("success.image.uploaded", null, LocaleUtils.getCurrentLocale()))
                .pathFile(audioUrl)
                .build());
    }

    @GetMapping("/images/{filename:.+}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> getLessonImage(@PathVariable String filename) throws IOException {
        Path filePath = lessonMediaService.getLessonImagePath(filename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "image/png";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(resource);
    }

    @GetMapping("/audios/{filename:.+}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> getLessonAudio(@PathVariable String filename) throws IOException {
        Path filePath = lessonMediaService.getLessonAudioPath(filename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "audio/mpeg";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(resource);
    }
}
