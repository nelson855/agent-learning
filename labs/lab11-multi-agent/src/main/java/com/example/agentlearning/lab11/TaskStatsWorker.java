package com.example.agentlearning.lab11;

import java.util.List;

/**
 * Worker A：任务统计。收到全部原始统计，输出结构化 JSON。
 */
public final class TaskStatsWorker implements Worker {

    private static final String PROMPT = """
            你是任务统计员（Worker A）。
            根据给定的本周任务系统原始统计，输出 JSON：
            {
              "totalTasks": 整数,
              "completedTasks": 整数,
              "failedTasks": 整数,
              "abnormalRatio": 小数(0~1),
              "avgDurationMinutes": 整数,
              "degradedTasks": 整数
            }
            只输出 JSON，不要多余文字。""";

    @Override
    public String name() {
        return "TaskStatsWorker";
    }

    @Override
    public String systemPrompt() {
        return PROMPT;
    }

    @Override
    public String buildContext(TaskStats stats) {
        return "本周任务原始统计:\n" + ReportJson.toJson(stats);
    }
}