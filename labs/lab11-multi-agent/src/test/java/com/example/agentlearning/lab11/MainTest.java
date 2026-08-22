package com.example.agentlearning.lab11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void comparisonScriptDrivesFourCallsInOrder() {
        FakeLlmClient llm = Comparison.scripted();
        TaskStats stats = new TaskStats(198, 180, 12, 0.076, 8, 3);

        GenerationOutcome single = new SingleAgentReportGenerator(llm).run(stats);
        GenerationOutcome multi = new MultiAgentReportGenerator(llm).run(stats, "分析");

        assertEquals(1, single.modelCalls());
        assertTrue(single.success());
        assertEquals(3, multi.modelCalls());
        assertTrue(multi.success());

        // 共享 FakeLlmClient 共消耗 4 条脚本
        assertEquals(4, llm.chatCalls());
    }
}