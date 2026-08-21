package com.example.agentlearning.lab10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Generator-Evaluator Loop 的关键行为（对应 Prompt 的「测试」一节）。
 *
 * <p>用 FakeLlmClient 按序注入回复，验证：
 * <ul>
 *   <li>结构错误 → ProgramValidator 直接拒绝，<b>Evaluator 不被调用</b>；</li>
 *   <li>结构正确但语义差 → 才进入 Evaluator → 反馈回灌后重试成功。</li>
 * </ul>
 */
class GeneratorEvaluatorLoopTest {

    private final TaskStats stats = new TaskStats(210, 182, 12, 0.071, 47, 3);

    private static String validReport(String summary, String... recs) {
        StringBuilder sb = new StringBuilder()
                .append("{\"week\":\"2026-W33\",\"totalTasks\":210,\"completedTasks\":182,")
                .append("\"failedTasks\":12,\"abnormalRatio\":0.071,\"avgDurationMinutes\":47,")
                .append("\"summary\":\"").append(summary).append("\",")
                .append("\"recommendations\":[");
        for (int i = 0; i < recs.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(recs[i]).append('"');
        }
        return sb.append("]}").toString();
    }

    // ------------------------------------------------------------------
    // Prompt 用例一：Fake Generator 第一次生成结构错误 → 直接拒绝，不调用 Evaluator
    // ------------------------------------------------------------------

    @Test
    void structuralErrorSkipsEvaluator() {
        // 只预设「漏掉 recommendations」的结构错误回复；Fake 队列空时复用最后一条，
        // 因此 3 次生成都是结构错误。
        String missingRecommendations = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"数据统计完成。"}""";
        FakeLlmClient fake = new FakeLlmClient().enqueue(missingRecommendations);

        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);
        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);

        assertFalse(result.accepted(), "结构始终错误，不应产出合格周报");
        assertEquals(0, result.evaluatorCalls(),
                "结构被 ProgramValidator 拒绝，Evaluator 一次都不该被调用");
        assertTrue(result.log().stream().allMatch(l -> !l.contains("EVALUATOR")),
                "日志不应出现任何 Evaluator 标记");
    }

    // ------------------------------------------------------------------
    // Prompt 用例二：结构正确但语义差 → 才进入 Evaluator
    // ------------------------------------------------------------------

    @Test
    void semanticFailureEntersEvaluatorThenRetries() {
        // 调用顺序：generate(差) → evaluate(拒) → generate(好) → evaluate(过)
        String poor = validReport("本周数据已经统计好了。", "改进系统");
        String feedbackReject =
                "{\"pass\":false,\"score\":1,\"issues\":[\"summary 未解释失败原因\",\"建议不可执行\"]}";
        String good = validReport(
                "本周 12 个失败，异常率 7.1%，集中在支付模块接口超时。",
                "为支付接口增加超时重试与熔断");
        String feedbackPass = "{\"pass\":true,\"score\":4,\"issues\":[]}";

        FakeLlmClient fake = new FakeLlmClient()
                .enqueue(poor, feedbackReject, good, feedbackPass);
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);

        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);

        assertTrue(result.accepted(), "结构合法+语义合格后应产出周报");
        assertEquals(2, result.evaluatorCalls(), "应恰好调用了 2 次 Evaluator（拒一次、过一次）");
        assertEquals(2, result.iterations(), "应在第 2 次迭代通过");
        assertTrue(result.log().stream().anyMatch(l -> l.contains("EVALUATOR REJECTED")),
                "日志应先出现 EVALUATOR REJECTED");
        assertTrue(result.log().stream().anyMatch(l -> l.contains("EVALUATOR PASSED")),
                "日志应出现 EVALUATOR PASSED");
    }

    // ------------------------------------------------------------------
    // 补充：反馈回灌确实发生（第二次生成收到了第一次的评估反馈）
    // ------------------------------------------------------------------

    @Test
    void feedbackIsFedBackIntoGenerator() {
        String poor = validReport("数据统计完成。", "优化");
        String feedbackReject = "{\"pass\":false,\"score\":1,\"issues\":[\"缺少失败原因\"]}";
        String good = validReport("12 个失败，集中在支付模块。", "为支付接口增加重试");
        String feedbackPass = "{\"pass\":true,\"score\":4,\"issues\":[]}";

        FakeLlmClient fake = new FakeLlmClient()
                .enqueue(poor, feedbackReject, good, feedbackPass);
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);
        loop.run(stats);

        // 第一次评估的反馈（"缺少失败原因"）应回灌进第二次生成请求的 user prompt
        boolean fedBack = fake.allRequests().stream()
                .flatMap(List::stream)
                .filter(m -> m.role() == Role.USER)
                .map(Message::content)
                .anyMatch(content -> content.contains("缺少失败原因"));
        assertTrue(fedBack, "评估反馈应回灌进第二次生成请求");
    }
}