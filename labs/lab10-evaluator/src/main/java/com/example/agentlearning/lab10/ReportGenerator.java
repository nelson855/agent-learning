package com.example.agentlearning.lab10;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator：根据 SQLite 任务统计生成一份 JSON 周报（未经校验的草稿）。
 *
 * <p>它只负责「生成」，不负责判断质量。判断质量由 {@link ProgramValidator}（结构）与
 * {@link LlmEvaluator}（语义）接手，形成 Generator-Evaluator Loop。模型输出的原始文本
 * 原样返回，交给 ProgramValidator 解析与校验。
 *
 * <p>历史反馈（结构错误 / 评估未通过）会被拼进后续 prompt，作为重试依据——这正是
 * Evaluator 的反馈回灌生成器的环节。
 */
public final class ReportGenerator {

    private static final String SYSTEM_PROMPT = """
            你是周报数据整理助手。根据给定的 SQLite 任务统计，生成一份 JSON 周报。
            必须包含以下字段，一个都不能少：
            - week: 字符串，ISO 周标识
            - totalTasks: 整数
            - completedTasks: 整数
            - failedTasks: 整数
            - abnormalRatio: 数字（异常占比 0~1）
            - avgDurationMinutes: 整数
            - summary: 字符串，覆盖整体结果并解释失败/异常原因与影响
            - recommendations: 字符串数组，至少一条且必须具体可执行
            只输出 JSON，不要多余文字，不要用 ``` 包裹。
            """;

    private final LlmClient llmClient;

    public ReportGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 生成一份周报草稿。
     *
     * @param stats        任务统计
     * @param priorFeedback 之前各轮的结构错误 / 评估未通过原因（可为空）
     */
    public String generate(TaskStats stats, List<String> priorFeedback) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.system(SYSTEM_PROMPT));
        messages.add(Message.user(buildGenerateRequest(stats, priorFeedback)));
        return llmClient.chat(messages).content();
    }

    private static String buildGenerateRequest(TaskStats stats, List<String> priorFeedback) {
        StringBuilder sb = new StringBuilder();
        String statsJson;
        try {
            statsJson = new ObjectMapper().writeValueAsString(Map.of(
                    "week", stats.week(),
                    "total_tasks", stats.totalTasks(),
                    "completed_tasks", stats.completedTasks(),
                    "failed_tasks", stats.failedTasks(),
                    "abnormal_ratio", stats.abnormalRatio(),
                    "avg_duration_minutes", stats.avgDurationMinutes(),
                    "degraded_tasks", stats.degradedTasks()));
        } catch (Exception e) {
            statsJson = "{}";
        }
        sb.append("任务统计: ").append(statsJson);
        if (priorFeedback != null && !priorFeedback.isEmpty()) {
            sb.append("\n\n之前的评审反馈，请在这次生成中修正:\n");
            int i = 1;
            for (String feedback : priorFeedback) {
                sb.append(i++).append(". ").append(feedback).append('\n');
            }
        }
        return sb.toString();
    }
}