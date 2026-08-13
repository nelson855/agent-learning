package com.example.agentlearning.lab01;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容 OpenAI Chat Completions 协议的 LLM 客户端。
 *
 * <p>配置从环境变量或仓库根目录 {@code .env} 文件读取（环境变量优先），不硬编码 Key：
 * <ul>
 *   <li>{@code LLM_BASE_URL}，例如 {@code https://api.openai.com/v1}</li>
 *   <li>{@code LLM_API_KEY}</li>
 *   <li>{@code LLM_MODEL}</li>
 * </ul>
 *
 * <p>使用 JDK 内置 {@link HttpClient} + Jackson，不引入任何供应商 SDK。
 */
public final class OpenAiCompatibleLlmClient implements LlmClient {

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(String baseUrl, String apiKey, String model) {
        this.baseUrl = requireNonBlank(baseUrl, "LLM_BASE_URL");
        this.apiKey = requireNonBlank(apiKey, "LLM_API_KEY");
        this.model = requireNonBlank(model, "LLM_MODEL");
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /** 从环境变量或根目录 .env 文件读取配置（环境变量优先）；缺失配置时快速失败。 */
    public static OpenAiCompatibleLlmClient fromConfig() {
        return new OpenAiCompatibleLlmClient(
                EnvFile.get("LLM_BASE_URL"),
                EnvFile.get("LLM_API_KEY"),
                EnvFile.get("LLM_MODEL"));
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatCompletionsUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody)))
                .build();

        HttpResponse<String> response = send(request);
        checkStatus(response);

        JsonNode root = parseJson(response.body());
        String content = root.at("/choices/0/message/content").asText();
        return new LlmResponse(content);
    }

    private String chatCompletionsUrl() {
        // 允许用户配置带或不带尾部斜杠的 baseUrl
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("序列化 LLM 请求失败", e);
        }
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("LLM 网络调用失败: " + baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 调用被中断", e);
        }
    }

    private void checkStatus(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("LLM HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException e) {
            throw new IllegalStateException("解析 LLM 响应失败", e);
        }
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少配置: " + name);
        }
        return value;
    }
}
