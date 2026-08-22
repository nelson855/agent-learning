package com.example.agentlearning.lab11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GeneratorTest {

    private TaskStats stats() {
        return new TaskStats(198, 180, 12, 0.076, 8, 3);
    }

    @Test
    void singleAgentUsesOneCallWithFullContext() {
        FakeLlmClient llm = new FakeLlmClient(
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3,"
                        + "\"mainFailures\":[\"超时\"],\"impact\":\"慢\",\"items\":[\"加重试\"]}");
        SingleAgentReportGenerator single = new SingleAgentReportGenerator(llm);

        GenerationOutcome out = single.run(stats());
        assertEquals(1, out.modelCalls());
        assertEquals(198, out.report().stats().totalTasks());
        assertTrue(out.success());
        // 单 Agent 一次请求包含全部统计与失败/建议内容
        assertTrue(llm.lastRequest().get(1).content().contains("totalTasks"));
    }

    @Test
    void singleAgentFailsWhenOutputMissingPieces() {
        FakeLlmClient llm = new FakeLlmClient(
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3}");
        SingleAgentReportGenerator single = new SingleAgentReportGenerator(llm);
        GenerationOutcome out = single.run(stats());
        assertFalse(out.success());
    }

    @Test
    void multiAgentUsesThreeCallsAndIsolatedContexts() {
        FakeLlmClient llm = new FakeLlmClient(
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3}",
                "{\"mainFailures\":[\"外部服务超时\"],\"impact\":\"慢\"}",
                "{\"items\":[\"加重试与熔断\"]}");
        MultiAgentReportGenerator multi = new MultiAgentReportGenerator(llm);

        GenerationOutcome out = multi.run(stats(), "分析");
        assertEquals(3, out.modelCalls());
        assertTrue(out.success());
        assertEquals(198, out.report().stats().totalTasks());
        assertEquals(1, out.report().recommendations().items().size());
    }

    @Test
    void singleAgentContextExceedsAnySingleWorker() {
        TaskStats stats = stats();
        String combined = "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3,"
                        + "\"mainFailures\":[\"a\"],\"impact\":\"i\",\"items\":[\"r\"]}";

        GenerationOutcome single = new SingleAgentReportGenerator(
                new FakeLlmClient(combined)).run(stats);

        // 单 Agent 的完整上下文，应大于任何一个只做局部任务的 Worker
        Worker statsWorker = new TaskStatsWorker();
        int statsWorkerChars = statsWorker.run(new FakeLlmClient(
                "{\"totalTasks\":1,\"completedTasks\":1,\"failedTasks\":0,"
                        + "\"abnormalRatio\":0.0,\"avgDurationMinutes\":1,\"degradedTasks\":0}"),
                stats).contextChars();

        assertTrue(single.contextChars() > 0);
        assertTrue(single.contextChars() > statsWorkerChars,
                "single(" + single.contextChars() + ") 应大于 statsWorker(" + statsWorkerChars + ")");
    }
}