package com.example.agentlearning.lab11;

import java.util.List;

/**
 * Version A：单 Agent 一次搞定全部内容。
 *
 * <p>把完整统计塞进一次调用，并要求模型一次性输出全部三类内容。
 * 优点：调用少、协调简单；代价：Context 大、职责混杂、单点失败。
 */
public final class SingleAgentReportGenerator {

    private static final String PROMPT = """
            你是任务系统运行分析员（单 Agent）。
            根据给定的完整任务统计，一次性输出一份 JSON，必须同时包含三块内容：
            1) 统计字段: totalTasks, completedTasks, failedTasks, abnormalRatio, avgDurationMinutes, degradedTasks
            2) 失败分析: mainFailures(数组,列出主要失败原因), impact(失败对业务的影响)
            3) 改进建议: items(数组,至少一条具体可执行的建议)
            只输出 JSON，不要多余文字。""";

    private final LlmClient llm;

    public SingleAgentReportGenerator(LlmClient llm) {
        this.llm = llm;
    }

    public GenerationOutcome run(TaskStats stats) {
        String request = "完整任务统计:\n" + ReportJson.toJson(stats);
        int contextChars = PROMPT.length() + request.length();

        String raw = llm.chat(List.of(Message.system(PROMPT), Message.user(request))).content();

        StatsReport s = ReportJson.parseStats(raw);
        FailureAnalysis f = ReportJson.parseFailure(raw);
        Recommendations r = ReportJson.parseRecommendations(raw);
        AggregatedReport report = new AggregatedReport(
                s == null ? StatsReport.empty() : s,
                f == null ? FailureAnalysis.empty() : f,
                r == null ? Recommendations.empty() : r);

        boolean success = Orchestrator.isComplete(report);
        return new GenerationOutcome(success ? 1 : 0, contextChars, success, report);
    }
}