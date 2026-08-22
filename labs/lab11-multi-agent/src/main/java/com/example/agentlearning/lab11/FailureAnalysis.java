package com.example.agentlearning.lab11;

import java.util.List;

/**
 * Worker B「失败原因分析」的结构化输出。
 */
public record FailureAnalysis(List<String> mainFailures, String impact) {

    public static FailureAnalysis empty() {
        return new FailureAnalysis(List.of(), "");
    }
}