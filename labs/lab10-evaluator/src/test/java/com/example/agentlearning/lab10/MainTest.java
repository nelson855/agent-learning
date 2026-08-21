package com.example.agentlearning.lab10;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Main / 端到端冒烟测试：完整 Pipeline（生成即通过）用 Fake 即可，不依赖真实模型或数据库。
 */
class MainTest {

    @Test
    void happyPathAccepted() {
        TaskStats stats = new TaskStats(210, 182, 12, 0.071, 47, 3);
        String goodReport = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"本周 12 个失败，异常率 7.1%，集中在支付模块接口超时。",
                 "recommendations":["为支付接口增加超时重试"]}""";
        String feedbackPass = "{\"pass\":true,\"score\":4,\"issues\":[]}";

        FakeLlmClient fake = new FakeLlmClient().enqueue(goodReport, feedbackPass);
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);

        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);

        assertTrue(result.accepted(), "结构合法且语义通过时应在首轮产出合格周报");
        assertTrue(result.log().stream().anyMatch(l -> l.contains("EVALUATOR PASSED")),
                "日志应出现 EVALUATOR PASSED");
    }
}