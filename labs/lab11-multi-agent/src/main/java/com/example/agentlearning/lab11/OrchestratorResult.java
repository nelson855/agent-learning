package com.example.agentlearning.lab11;

/**
 * Orchestrator 的一次执行结果。
 *
 * @param modelCalls   触发的模型调用次数（= Worker 数量）
 * @param contextChars 所有 Worker 累计发往模型的上下文字符数
 * @param success      合并后的报告是否三块内容齐全
 * @param report       合并后的汇报
 */
public record OrchestratorResult(int modelCalls, int contextChars, boolean success, AggregatedReport report) {
}