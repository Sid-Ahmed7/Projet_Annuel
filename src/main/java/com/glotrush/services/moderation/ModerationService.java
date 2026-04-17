package com.glotrush.services.moderation;

import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    @Value("${huggingface.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public boolean isContentToxic(String comment) {
        if (comment == null || comment.isBlank()) {
            log.warn("Any comment");
            return false;
        }

        log.info("Comment: '{}'", comment);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of("inputs", comment);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<List> response = restTemplate.exchange(
                "https://router.huggingface.co/hf-inference/models/unitary/multilingual-toxic-xlm-roberta",
                HttpMethod.POST,
                request,
                List.class
            );

            log.info("Response : {}", response.getStatusCode());

            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.warn("Response is null");
                return false;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get(0);
            if (results == null || results.isEmpty()) {
                log.warn("No results in response");
                return false;
            }

            Map<String, Object> topResult = results.stream()
                .max((a, b) -> Double.compare(
                    ((Number) a.get("score")).doubleValue(),
                    ((Number) b.get("score")).doubleValue()
                ))
                .orElse(null);

            if (topResult == null) return false;

            String label = (String) topResult.get("label");
            double score = ((Number) topResult.get("score")).doubleValue();
            log.info("Top label: {}, score: {}", label, score);

            boolean flagged = "toxic".equals(label) && score > 0.7;
            log.info("Content : {}", flagged);
            return flagged;

        } catch (Exception e) {
            log.error(" error: {}", e.getMessage());
            return false;
        }
    }
}
