package com.example.agentlearning.lab10;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * 第一层：确定性校验（Program Validator）。
 *
 * <p>凡是<b>能由程序确定的判断</b>（JSON 是否可解析、必填字段、数字类型、集合非空），
 * 都在这层完成，因此<b>不会调用模型</b>。只有结构合法才会把 {@link WeeklyReport}
 * 交给下一层的 LLM Evaluator 做语义判断。
 *
 * <pre>
 * 校验项：
 *   1. JSON 可解析（容忍 ```json 代码块包裹）
 *   2. required fields 存在：week / totalTasks / completedTasks / summary / recommendations
 *   3. 数字字段类型正确：totalTasks / completedTasks / failedTasks
 *   4. recommendations 非空
 * </pre>
 */
public final class ProgramValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 校验结果：通过与否 + 错误列表；合法时附带解析出的 {@link WeeklyReport}。 */
    public record ValidationResult(boolean valid, List<String> errors, WeeklyReport report) {

        static ValidationResult rejected(List<String> errors) {
            return new ValidationResult(false, errors, null);
        }

        static ValidationResult accepted(WeeklyReport report) {
            return new ValidationResult(true, List.of(), report);
        }
    }

    /**
     * 对一个 LLM 生成的原始文本做确定性校验。
     *
     * @param rawText 模型输出的周报文本（允许带 ```json 包裹）
     */
    public ValidationResult validate(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return ValidationResult.rejected(List.of("JSON 为空"));
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(stripCodeFence(rawText));
        } catch (Exception e) {
            return ValidationResult.rejected(List.of("JSON 无法解析: " + e.getMessage()));
        }

        List<String> errors = new ArrayList<>();

        requireString(root, "week", errors);
        requireNumber(root, "totalTasks", errors);
        requireNumber(root, "completedTasks", errors);
        requireNumber(root, "failedTasks", errors);
        requireString(root, "summary", errors);
        requireNonEmptyArray(root, "recommendations", errors);

        if (!errors.isEmpty()) {
            return ValidationResult.rejected(errors);
        }

        try {
            return ValidationResult.accepted(objectMapper.treeToValue(root, WeeklyReport.class));
        } catch (Exception e) {
            return ValidationResult.rejected(List.of("无法反序列化为 WeeklyReport: " + e.getMessage()));
        }
    }

    private static String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNl = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNl >= 0 && lastFence > firstNl) {
                return trimmed.substring(firstNl + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private static void requireString(JsonNode root, String field, List<String> errors) {
        JsonNode node = root.get(field);
        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            errors.add("必填字段缺失或为空: " + field);
        }
    }

    private static void requireNumber(JsonNode root, String field, List<String> errors) {
        JsonNode node = root.get(field);
        if (node == null || !node.isNumber()) {
            errors.add("数字字段缺失或类型错误: " + field);
        }
    }

    private static void requireNonEmptyArray(JsonNode root, String field, List<String> errors) {
        JsonNode node = root.get(field);
        if (node == null || !node.isArray() || node.isEmpty()) {
            errors.add("数组字段缺失或为空: " + field);
        }
    }
}