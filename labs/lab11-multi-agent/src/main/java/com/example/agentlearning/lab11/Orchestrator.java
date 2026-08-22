package com.example.agentlearning.lab11;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator：选择 Worker → 调用收集 → 合并为 {@link AggregatedReport}。
 *
 * <p>教学重点：
 * <ol>
 *   <li><b>选择</b>：{@link #decideWorkers} 按任务目标决定启用哪些 Worker；</li>
 *   <li><b>收集</b>：每个 Worker 拿到「自己的上下文片段」，输入输出结构化；</li>
 *   <li><b>合并</b>：{@link #merge} 把多份结构化输出组装成统一报告。</li>
 * </ol>
 *
 * <p>禁止：Worker 之间自由互聊、无限递归创建子 Worker。
 */
public final class Orchestrator {

    private final LlmClient llm;
    private final List<Worker> workers;

    public Orchestrator(LlmClient llm) {
        this.llm = llm;
        this.workers = List.of(
                new TaskStatsWorker(),
                new FailureAnalysisWorker(),
                new RecommendationWorker());
    }

    /** 按任务目标决定启用哪些 Worker（本 demo 固定三个，但保留"决策"这一步）。 */
    public List<Worker> decideWorkers(String goal) {
        return workers;
    }

    /** 调用选定 Worker 收集结果并合并。 */
    public OrchestratorResult run(TaskStats stats, String goal) {
        List<WorkerResult> results = new ArrayList<>();
        int calls = 0;
        int chars = 0;
        for (Worker worker : decideWorkers(goal)) {
            WorkerResult wr = worker.run(llm, stats);
            results.add(wr);
            calls++;
            chars += wr.contextChars();
        }
        AggregatedReport report = merge(results);
        boolean success = isComplete(report);
        return new OrchestratorResult(calls, chars, success, report);
    }

    /** 汇集后的报告是否三块都有内容（用于判定本次 Multi-Agent 是否成功）。 */
    static boolean isComplete(AggregatedReport report) {
        return report.stats().totalTasks() > 0
                && !report.failure().mainFailures().isEmpty()
                && !report.recommendations().items().isEmpty();
    }

    /** 把 Worker A/B/C 的输出确定性合并为一份报告；解析失败项置空，不崩溃。 */
    static AggregatedReport merge(List<WorkerResult> results) {
        StatsReport stats = StatsReport.empty();
        FailureAnalysis failure = FailureAnalysis.empty();
        Recommendations recs = Recommendations.empty();

        for (WorkerResult wr : results) {
            StatsReport s = ReportJson.parseStats(wr.raw());
            if (s != null) {
                stats = s;
            }
            FailureAnalysis f = ReportJson.parseFailure(wr.raw());
            if (f != null) {
                failure = f;
            }
            Recommendations r = ReportJson.parseRecommendations(wr.raw());
            if (r != null) {
                recs = r;
            }
        }
        return new AggregatedReport(stats, failure, recs);
    }
}