package com.example.agentlearning.stage01;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 从模型输出的一段 JSON 里取一个字段（最小工具）。
 *
 * <p>模型按约定只输出 {@code {"字段名":"值"}} 这种单字段 JSON，我们据此取字段。
 */
public final class JsonExtract {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonExtract() {
    }

    public static String field(String json, String key) {
        try {
            JsonNode root = MAPPER.readTree(json);
            return root.path(key).asText("");
        } catch (Exception e) {
            throw new IllegalArgumentException("无法从模型输出中解析字段 " + key + ": " + json, e);
        }
    }
}
