package com.glotrush.services.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.utils.LocaleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "mistral")
public class MistralAIService implements IAIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Value("${ai.mistral.api-key}")
    private String apiKey;

    @Value("${ai.mistral.url:https://api.mistral.ai/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.mistral.model:mistral-small-latest}")
    private String model;

    @Override
    public <T> T generateJsonContent(String prompt, Class<T> responseClass) {
        String rawResponse = callMistralApi(prompt, true);
        try {
            return objectMapper.readValue(rawResponse, responseClass);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la désérialisation de la réponse AI en {}. Réponse brute : {}", responseClass.getSimpleName(), rawResponse, e);
            throw new RuntimeException(messageSource.getMessage("error.ai.invalid_response_format", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    @Override
    public String generateRawContent(String prompt) {
        return callMistralApi(prompt, false);
    }

    private String callMistralApi(String prompt, boolean jsonMode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", jsonMode ? Map.of("type", "json_object") : Map.of("type", "text")
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            throw new RuntimeException(messageSource.getMessage("error.ai.empty_response", null, LocaleUtils.getCurrentLocale()));
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Mistral AI", e);
            throw new RuntimeException(messageSource.getMessage("error.ai.generation_failed", null, LocaleUtils.getCurrentLocale()), e);
        }
    }
}
