package com.example.agentlearning.lab10;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * 第二层：LLM Evaluator —— 只判断语义。
 *
 * <p>输入已经通过 {@link ProgramValidator} 的结构校验，这里用模型判断两类<b>难以用代码
 * 编码</b>的标准：
 * <ul>
 *   <li>{@code summary} 是否解释了异常（失败原因 / 影响）；</li>
 *   <li>{@code recommendations} 是否可执行。</li>
 * </ul>
 *
 * <p>模型按 system prompt 约定返回结构化 JSON：
 * <pre>
 * {"pass": false, "score": 3, "issues": ["没有说明失败原因"]}
 * </pre>
 * 若评估输出自身无法解析，按「不通过」处理并给出确定性问题，避免坏输出被放行。
 */
public final class LlmEvaluator {

    private static final String SYSTEM_PROMPT = """
            你是一名周报质量评审（Evaluator）。只做语义判断，不重新生成周报。
            依据以下两条 Rubric 打 0~2 分：
            - 完整性：summary 是否解释了本周异常（失败原因与影响）；
            - 可执行性：recommendations 是否具体、可落地、可行。
            满分 4。分数 >= 3 即通过。
            只输出 JSON，不要多余文字：
            {"pass": true 或 false, "score": 整数, "issues": ["未达标的具体问题，可为空数组"]}
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmEvaluator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 判断一份已通过结构校验的周报。
     *
     * @param stats  作为语义背景的任务统计
     * @param report 待评审的周报
     */
    public EvaluatorFeedback evaluate(TaskStats stats, WeeklyReport report) {
        String reply = llmClient.chat(List.of(
                Message.system(SYSTEM_PROMPT),
                Message.user(buildEvalRequest(stats, report)))).content();

        try {
            JsonNode root = objectMapper.readTree(reply);
            boolean pass = root.path("pass").asBoolean(false);
            int score = root.path("score").asInt(0);
            List<String> issues = objectMapper.convertValue(
                    root.path("issues"),
                    new TypeReference<List<String>>() { });
            return new EvaluatorFeedback(pass, score, issues);
        } catch (Exception e) {
            return EvaluatorFeedback.failed(0, "评估输出无法解析为 JSON: " + e.getMessage());
        }
    }

    private static String buildEvalRequest(TaskStats stats, WeeklyReport report) {
        String statsJson;
        try {
            statsJson = new ObjectMapper().writeValueAsString(Map.of(
                    "total_tasks", stats.totalTasks(),
                    "completed_tasks", stats.completedTasks(),
                    "failed_tasks", stats.failedTasks(),
                    "abnormal_ratio", stats.abnormalRatio(),
                    "degraded_tasks", stats.degradedTasks()));
        } catch (Exception e) {
            statsJson = "{}";
        }
        return "任务统计: " + statsJson + "\n\n待评审周报:\n" + report;
    }
}