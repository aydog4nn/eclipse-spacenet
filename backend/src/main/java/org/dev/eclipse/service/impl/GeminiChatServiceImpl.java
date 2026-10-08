package org.dev.eclipse.service.impl;

import tools.jackson.databind.JsonNode;
import org.dev.eclipse.config.GeminiProperties;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.service.GeminiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiChatServiceImpl implements GeminiChatService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiChatServiceImpl.class);

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;

    public GeminiChatServiceImpl(RestClient.Builder restClientBuilder, GeminiProperties geminiProperties) {
        this.restClient = restClientBuilder.build();
        this.geminiProperties = geminiProperties;
    }

    @Override
    public String generateReply(List<ChatMessage> history) {
        if (!StringUtils.hasText(geminiProperties.getApiKey())) {
            throw new BadRequestException("Gemini API key tanimli degil. GEMINI_API_KEY eklemelisiniz.");
        }

        List<Map<String, Object>> contents = history.stream()
                .map(message -> Map.of(
                        "role", message.role(),
                        "parts", List.of(Map.of("text", message.text()))
                ))
                .toList();

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("system_instruction", Map.of(
                "parts", List.of(Map.of("text", geminiProperties.getSystemPrompt()))
        ));
        requestBody.put("generationConfig", Map.of(
                "temperature", 0.6,
                "topP", 0.9,
                "maxOutputTokens", 1024
        ));
        requestBody.put("contents", contents);

        JsonNode response = restClient.post()
                .uri(geminiProperties.getApiUrl(), geminiProperties.getModel())
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", geminiProperties.getApiKey())
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        String text = extractText(response);
        logger.info("Gemini response generated with model {}", geminiProperties.getModel());
        return text;
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new BadRequestException("Gemini response bos dondu.");
        }

        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new BadRequestException("Gemini uygun bir yanit donmedi.");
        }

        StringBuilder builder = new StringBuilder();
        JsonNode parts = candidates.get(0).path("content").path("parts");

        if (parts.isArray()) {
            for (JsonNode part : parts) {
                String text = part.path("text").asText();
                if (StringUtils.hasText(text)) {
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(text);
                }
            }
        }

        if (!builder.isEmpty()) {
            return builder.toString();
        }

        throw new BadRequestException("Gemini yanitinda metin bulunamadi.");
    }
}
