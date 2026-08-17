package com.example.agentlearning.lab08;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容的 HTTP 客户端：POST {@code {baseUrl}/chat/completions}，
 * 读取 {@code choices[0].message.content}。
 */
public final class OpenAiCompatibleLlmClient implements LlmClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final HttpClient http;

    public OpenAiCompatibleLlmClient(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static OpenAiCompatibleLlmClient fromEnv(Map<String, String> env) {
        return new OpenAiCompatibleLlmClient(
                env.get(EnvFile.BASE_URL),
                env.get(EnvFile.API_KEY),
                env.get(EnvFile.MODEL));
    }

    public static boolean isConfigured(Map<String, String> env) {
        return env.get(EnvFile.BASE_URL) != null
                && env.get(EnvFile.API_KEY) != null
                && env.get(EnvFile.MODEL) != null;
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        try {
            List<JsonNode> messageNodes = messages.stream()
                    .map(m -> jsonStringToNode(m.toJson()))
                    .collect(Collectors.toList());
            String body = MAPPER.writeValueAsString(Map.of("model", model, "messages", messageNodes));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("LLM 请求失败 HTTP " + response.statusCode() + ": " + response.body());
            }
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return new LlmResponse(content);
        } catch (Exception e) {
            throw new IllegalStateException("LLM 调用失败", e);
        }
    }

    private static JsonNode jsonStringToNode(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 解析失败", e);
        }
    }
}
