package com.example.agentlearning.lab11;

/**
 * Worker B：失败原因分析（只看到失败/退化部分，看不到成功任务）。
 */
public final class FailureAnalysisWorker implements Worker {

    private static final String PROMPT = """
            你是失败原因分析员（Worker B）。
            根据本周任务的失败与退化数据，输出 JSON：
            {
              "mainFailures": [字符串数组，列出主要失败原因],
              "impact": "字符串，说明这些失败对整体业务的影响"
            }
            只输出 JSON，不要多余文字。""";

    @Override
    public String name() {
        return "FailureAnalysisWorker";
    }

    @Override
    public String systemPrompt() {
        return PROMPT;
    }

    @Override
    public String buildContext(TaskStats stats) {
        return "失败/退化数据：\n"
                + "- 失败任务数: " + stats.failedTasks() + "\n"
                + "- 退化/超时任务数: " + stats.degradedTasks() + "\n"
                + "- 异常占比: " + String.format("%.1f%%", stats.abnormalRatio() * 100) + "\n"
                + "- 平均执行时长: " + stats.avgDurationMinutes() + " 分钟\n"
                + "注意：你只能看到失败与退化数据，不查看成功任务的具体信息。";
    }
}