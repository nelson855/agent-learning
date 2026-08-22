package com.example.agentlearning.lab11;

/**
 * 一次生成（Single 或 Multi）的对照指标与产物。
 *
 * @param modelCalls   模型调用次数
 * @param contextChars 累计发往模型的上下文字符数
 * @param success      是否产出完整报告
 * @param report       汇报
 */
public record GenerationOutcome(int modelCalls, int contextChars, boolean success, AggregatedReport report) {
}