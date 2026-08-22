package com.example.agentlearning.lab11;

/**
 * SQLite 聚合出的任务统计（本周任务系统运行情况）。
 *
 * @param totalTasks         总任务数
 * @param completedTasks     已完成数
 * @param failedTasks        失败数
 * @param abnormalRatio      异常占比（0~1）
 * @param avgDurationMinutes 平均时长（分钟）
 * @param degradedTasks      退化/超时数
 */
public record TaskStats(
        int totalTasks,
        int completedTasks,
        int failedTasks,
        double abnormalRatio,
        int avgDurationMinutes,
        int degradedTasks) {

    public String week() {
        return "2026-W33";
    }
}