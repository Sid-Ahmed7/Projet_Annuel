package com.glotrush.services.moderation;

import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.glotrush.constants.ApiConstants;
import com.glotrush.constants.ModerationConstants;

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

    public boolean isBlocklisted(String comment) {
        if (comment == null || comment.isBlank()){
            return false;
        }
        String lowerComment = comment.toLowerCase();
        boolean blocked = ModerationConstants.FRENCH_INSULT_BLOCK.stream().anyMatch(lowerComment::contains);
    
        return blocked;
    }

    public Double getToxicityScore(String comment) {
        if (comment == null || comment.isBlank()) {
            log.warn("Any comment");
            return null;
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
                ApiConstants.API_URL,
                HttpMethod.POST,
                request,
                List.class
            );

            log.info("Response : {}", response.getStatusCode());

            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.warn("Response is null");
                return null;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get(0);
            if (results == null || results.isEmpty()) {
                log.warn("No results in response");
                return null;
            }

           Double score = results.stream().filter(review -> "toxic".equals(review.get("label"))).findFirst()
                    .map(review -> ((Number) review.get("score")).doubleValue())
                    .orElse(null);
            log.info("Toxicity score: {}", score);
            return score;

        } catch (Exception e) {
            log.error(" error: {}", e.getMessage());
            return null;
        }
    }
}
