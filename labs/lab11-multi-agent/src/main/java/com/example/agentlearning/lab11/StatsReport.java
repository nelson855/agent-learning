package com.example.agentlearning.lab11;

/**
 * Worker A「任务统计」的结构化输出。
 */
public record StatsReport(
        int totalTasks,
        int completedTasks,
        int failedTasks,
        double abnormalRatio,
        int avgDurationMinutes,
        int degradedTasks) {

    public static StatsReport empty() {
        return new StatsReport(0, 0, 0, 0.0, 0, 0);
    }
}