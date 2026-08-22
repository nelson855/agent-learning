package com.example.agentlearning.lab11;

import java.util.List;

/**
 * Worker C「改进建议」的结构化输出。
 */
public record Recommendations(List<String> items) {

    public static Recommendations empty() {
        return new Recommendations(List.of());
    }
}