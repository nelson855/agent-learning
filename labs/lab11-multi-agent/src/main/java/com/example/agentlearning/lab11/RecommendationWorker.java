package com.example.agentlearning.lab11;

import java.util.List;

/**
 * Worker C：改进建议（只看到异常概况与时长，不接触具体任务数）。
 */
public final class RecommendationWorker implements Worker {

    private static final String PROMPT = """
            你是改进建议顾问（Worker C）。
            根据本周任务系统的异常概况，输出 JSON：
            {
              "items": [字符串数组，至少一条具体、可执行的改进建议]
            }
            只输出 JSON，不要多余文字。""";

    @Override
    public String name() {
        return "RecommendationWorker";
    }

    @Override
    public String systemPrompt() {
        return PROMPT;
    }

    @Override
    public String buildContext(TaskStats stats) {
        return "异常概况（你只能看到以下信息，不接触具体任务数）：\n"
                + "- 异常占比: " + String.format("%.1f%%", stats.abnormalRatio() * 100) + "\n"
                + "- 平均执行时长: " + stats.avgDurationMinutes() + " 分钟\n"
                + "- 退化/超时任务数: " + stats.degradedTasks() + "\n"
                + "- 失败任务数: " + stats.failedTasks();
    }
}