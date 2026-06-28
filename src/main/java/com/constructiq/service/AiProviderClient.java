package com.constructiq.service;

import com.constructiq.config.AiProperties;
import com.constructiq.exception.AiProviderException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AiProviderClient {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiProviderClient(
            AiProperties properties,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .build();
    }

    public String getChatModel() {
        return properties.getChatModel();
    }

    public String getEmbedModel() {
        return properties.getEmbedModel();
    }

    public String chat(List<ChatMessage> messages) {
        Map<String, Object> request = buildChatRequest(messages);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(properties.getChatEndpoint())
                    .headers(headers -> setAuthHeader(headers, properties.getApiKey()))
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return extractChatContent(response);
        } catch (RestClientException ex) {
            throw new AiProviderException("AI chat model is unavailable", ex);
        }
    }

    public List<Double> embed(String text) {
        if (!properties.isEmbeddingsEnabled()) {
            throw new AiProviderException("AI embeddings are disabled");
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(properties.getEmbedEndpoint())
                    .headers(headers -> setAuthHeader(headers, properties.getApiKey()))
                    .body(buildEmbeddingRequest(text))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return extractEmbedding(response);
        } catch (RestClientException legacyEx) {
            return embedWithFallbackEndpoint(text, legacyEx);
        }
    }

    private Map<String, Object> buildChatRequest(List<ChatMessage> messages) {
        if (isOpenAiFormat(properties.getChatFormat())) {
            return Map.of(
                    "model", properties.getChatModel(),
                    "messages", messages,
                    "stream", false,
                    "temperature", properties.getTemperature()
            );
        }

        return Map.of(
                "model", properties.getChatModel(),
                "messages", messages,
                "stream", false,
                "options", Map.of("temperature", properties.getTemperature())
        );
    }

    private Map<String, Object> buildEmbeddingRequest(String text) {
        if (isOpenAiFormat(properties.getEmbedFormat())) {
            return Map.of(
                    "model", properties.getEmbedModel(),
                    "input", text
            );
        }

        return Map.of(
                "model", properties.getEmbedModel(),
                "prompt", text
        );
    }

    private List<Double> embedWithFallbackEndpoint(String text, RestClientException originalException) {
        String fallbackEndpoint = properties.getEmbedFallbackEndpoint();
        if (fallbackEndpoint == null || fallbackEndpoint.isBlank()
                || fallbackEndpoint.equals(properties.getEmbedEndpoint())) {
            throw new AiProviderException("AI embedding model is unavailable", originalException);
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(fallbackEndpoint)
                    .headers(headers -> setAuthHeader(headers, properties.getApiKey()))
                    .body(Map.of(
                            "model", properties.getEmbedModel(),
                            "input", text
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return extractEmbedding(response);
        } catch (RestClientException ex) {
            ex.addSuppressed(originalException);
            throw new AiProviderException("AI embedding model is unavailable", ex);
        }
    }

    private String extractChatContent(Map<String, Object> response) {
        if (response == null) {
            throw new AiProviderException("AI chat model returned an empty response");
        }

        Object messageValue = response.get("message");
        if (messageValue instanceof Map<?, ?> message) {
            Object content = message.get("content");
            if (content instanceof String text && !text.isBlank()) {
                return text.trim();
            }
        }

        Object responseValue = response.get("response");
        if (responseValue instanceof String text && !text.isBlank()) {
            return text.trim();
        }

        Object choicesValue = response.get("choices");
        if (choicesValue instanceof List<?> choices && !choices.isEmpty()
                && choices.get(0) instanceof Map<?, ?> choice) {
            Object openAiMessageValue = choice.get("message");
            if (openAiMessageValue instanceof Map<?, ?> openAiMessage) {
                Object content = openAiMessage.get("content");
                if (content instanceof String text && !text.isBlank()) {
                    return text.trim();
                }
            }

            Object textValue = choice.get("text");
            if (textValue instanceof String text && !text.isBlank()) {
                return text.trim();
            }
        }

        throw new AiProviderException("AI chat model returned an invalid response");
    }

    private List<Double> extractEmbedding(Map<String, Object> response) {
        if (response == null) {
            throw new AiProviderException("AI embedding model returned an empty response");
        }

        Object embeddingValue = response.get("embedding");
        if (embeddingValue == null) {
            embeddingValue = response.get("embeddings");
        }
        if (embeddingValue == null) {
            embeddingValue = extractOpenAiEmbedding(response);
        }

        if (embeddingValue instanceof List<?> embeddingList
                && !embeddingList.isEmpty()
                && embeddingList.get(0) instanceof List<?>) {
            embeddingValue = embeddingList.get(0);
        }

        if (!(embeddingValue instanceof List<?>)) {
            throw new AiProviderException("AI embedding model returned an invalid response");
        }

        List<Double> embedding = objectMapper.convertValue(
                embeddingValue,
                new TypeReference<List<Double>>() {
                }
        );

        if (embedding.isEmpty()) {
            throw new AiProviderException("AI embedding model returned an empty vector");
        }

        return new ArrayList<>(embedding);
    }

    private Object extractOpenAiEmbedding(Map<String, Object> response) {
        Object dataValue = response.get("data");
        if (!(dataValue instanceof List<?> data) || data.isEmpty()
                || !(data.get(0) instanceof Map<?, ?> item)) {
            return null;
        }

        return item.get("embedding");
    }

    private void setAuthHeader(org.springframework.http.HttpHeaders headers, String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            headers.setBearerAuth(apiKey.trim());
        }
    }

    private boolean isOpenAiFormat(String format) {
        return "openai".equalsIgnoreCase(format)
                || "openai-compatible".equalsIgnoreCase(format);
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalizedHost = baseUrl == null || baseUrl.isBlank()
                ? "http://localhost:11434"
                : baseUrl.trim();

        while (normalizedHost.endsWith("/")) {
            normalizedHost = normalizedHost.substring(0, normalizedHost.length() - 1);
        }

        return normalizedHost;
    }

    public record ChatMessage(String role, String content) {
    }
}
