package com.example.agentlearning.lab11;

/**
 * 汇总后的任务系统运行分析（Orchestrator 合并产物，也是 Single-Agent 的目标输出）。
 */
public record AggregatedReport(
        StatsReport stats,
        FailureAnalysis failure,
        Recommendations recommendations) {

    public static AggregatedReport empty() {
        return new AggregatedReport(StatsReport.empty(), FailureAnalysis.empty(), Recommendations.empty());
    }
}