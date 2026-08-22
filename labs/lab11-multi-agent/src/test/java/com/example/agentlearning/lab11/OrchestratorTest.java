package com.example.agentlearning.lab11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrchestratorTest {

    private TaskStats stats() {
        return new TaskStats(198, 180, 12, 0.076, 8, 3);
    }

    @Test
    void decidesThreeWorkers() {
        Orchestrator o = new Orchestrator(new FakeLlmClient());
        assertEquals(3, o.decideWorkers("分析").size());
    }

    @Test
    void mergeCombinesThreeWorkerOutputs() {
        WorkerResult stats = new WorkerResult("A", 0,
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3}");
        WorkerResult failure = new WorkerResult("B", 0,
                "{\"mainFailures\":[\"超时\",\"校验失败\"],\"impact\":\"拖慢平均时长\"}");
        WorkerResult rec = new WorkerResult("C", 0,
                "{\"items\":[\"加重试与熔断\",\"提前校验数据\"]}");

        AggregatedReport report = Orchestrator.merge(List.of(stats, failure, rec));
        assertEquals(198, report.stats().totalTasks());
        assertEquals(List.of("超时", "校验失败"), report.failure().mainFailures());
        assertEquals(2, report.recommendations().items().size());
        assertTrue(Orchestrator.isComplete(report));
    }

    @Test
    void runCallsEachWorkerOnceAndUsesIsolatedContext() {
        FakeLlmClient llm = new FakeLlmClient(
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3}",
                "{\"mainFailures\":[\"超时\"],\"impact\":\"慢\"}",
                "{\"items\":[\"加重试\"]}");
        Orchestrator o = new Orchestrator(llm);

        OrchestratorResult r = o.run(stats(), "分析");
        assertEquals(3, r.modelCalls());
        assertTrue(r.success());

        // 三个 Worker 收到的是不同上下文（隔离）
        assertEquals(3, llm.chatCalls());
        List<List<Message>> reqs = llm.allRequests();
        assertNotEquals(reqs.get(0).get(1).content(), reqs.get(1).get(1).content());
        assertFalse(reqs.get(1).get(1).content().contains("totalTasks"));
        assertFalse(reqs.get(2).get(1).content().contains("totalTasks"));
    }

    @Test
    void mergeToleratesOneBrokenOutput() {
        WorkerResult stats = new WorkerResult("A", 0, "not json");
        WorkerResult failure = new WorkerResult("B", 0,
                "{\"mainFailures\":[\"超时\"],\"impact\":\"慢\"}");
        WorkerResult rec = new WorkerResult("C", 0, "{\"items\":[\"加重试\"]}");
        AggregatedReport report = Orchestrator.merge(List.of(stats, failure, rec));
        assertEquals(0, report.stats().totalTasks());           // 坏输出置空
        assertFalse(report.failure().mainFailures().isEmpty()); // 好的仍保留
        assertFalse(report.recommendations().items().isEmpty());
    }
}