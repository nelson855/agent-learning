package com.example.agentlearning.lab11;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * Worker 输出 JSON 的确定性解析工具。
 * 解析失败返回 {@code null}，由调用方决定如何处理。
 */
final class ReportJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ReportJson() {
    }

    static String stripCodeFence(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            int firstNl = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNl >= 0 && lastFence > firstNl) {
                return trimmed.substring(firstNl + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    static StatsReport parseStats(String raw) {
        try {
            JsonNode root = MAPPER.readTree(stripCodeFence(raw));
            if (!root.has("totalTasks")) {
                return null;
            }
            return new StatsReport(
                    root.path("totalTasks").asInt(0),
                    root.path("completedTasks").asInt(0),
                    root.path("failedTasks").asInt(0),
                    root.path("abnormalRatio").asDouble(0.0),
                    root.path("avgDurationMinutes").asInt(0),
                    root.path("degradedTasks").asInt(0));
        } catch (Exception e) {
            return null;
        }
    }

    static FailureAnalysis parseFailure(String raw) {
        try {
            JsonNode root = MAPPER.readTree(stripCodeFence(raw));
            if (!root.has("mainFailures")) {
                return null;
            }
            return new FailureAnalysis(
                    asStringList(root, "mainFailures"),
                    root.path("impact").asText(""));
        } catch (Exception e) {
            return null;
        }
    }

    static Recommendations parseRecommendations(String raw) {
        try {
            JsonNode root = MAPPER.readTree(stripCodeFence(raw));
            if (!root.has("items")) {
                return null;
            }
            return new Recommendations(asStringList(root, "items"));
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> asStringList(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null) {
            return List.of();
        }
        if (node.isArray()) {
            try {
                return MAPPER.convertValue(node, new TypeReference<List<String>>() {
                });
            } catch (Exception e) {
                return List.of(node.toString());
            }
        }
        if (node.isTextual()) {
            return List.of(node.asText());
        }
        return List.of();
    }

    static String toJson(TaskStats stats) {
        try {
            return MAPPER.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}