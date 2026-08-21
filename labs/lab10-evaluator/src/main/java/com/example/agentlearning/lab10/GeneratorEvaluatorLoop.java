package com.example.agentlearning.lab10;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator-Evaluator Loop 编排器。
 *
 * <pre>
 * Generator → ProgramValidator → LlmEvaluator → Pass?
 *                                             ├─ Yes → 返回合格周报
 *                                             └─ No  → feedback 回灌 Generator 重试
 * </pre>
 *
 * <p>关键约束（对应教材 11.4 / CLAUDE.md 7.3）：
 * <ul>
 *   <li><b>确定性校验优先</b>：ProgramValidator 没通过（JSON 非法 / 缺必填 / 类型错 /
 *       recommendations 为空）时，<b>不会调用 Evaluator</b>，直接把结构错误回灌给 Generator；</li>
 *   <li><b>必须限制次数</b>：{@link #MAX_ITERATIONS} 封顶，防止模型互相否定无限循环。</li>
 * </ul>
 *
 * <p>{@link LoopResult} 记录评估器调用次数与逐轮日志，便于 Main / 测试观察。
 */
public final class GeneratorEvaluatorLoop {

    /** 最大迭代次数：Generator 最多生成 {@code MAX_ITERATIONS} 次、随之最多评估这么多次。 */
    public static final int MAX_ITERATIONS = 3;

    private final ReportGenerator generator;
    private final ProgramValidator validator;
    private final LlmEvaluator evaluator;

    public GeneratorEvaluatorLoop(LlmClient llmClient) {
        this.generator = new ReportGenerator(llmClient);
        this.validator = new ProgramValidator();
        this.evaluator = new LlmEvaluator(llmClient);
    }

    /** 单次运行结果：合格周报（可能为 null）+ 迭代/评估次数 + 可观察日志。 */
    public record LoopResult(
            WeeklyReport report,
            int iterations,
            int evaluatorCalls,
            List<String> log) {

        /** 是否最终产出了通过评审的合格周报。 */
        public boolean accepted() {
            return report != null;
        }
    }

    /**
     * 对一份任务统计运行完整 Pipeline。
     *
     * @param stats 来自 SQLite 聚合的任务统计
     */
    public LoopResult run(TaskStats stats) {
        List<String> log = new ArrayList<>();
        List<String> priorFeedback = new ArrayList<>();
        int evaluatorCalls = 0;

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            log.add("[iter " + i + "] GENERATE");
            String raw = generator.generate(stats, priorFeedback);

            ProgramValidator.ValidationResult validation = validator.validate(raw);
            if (!validation.valid()) {
                String detail = String.join("; ", validation.errors());
                log.add("[iter " + i + "] PROGRAM VALIDATION FAILED -> " + detail);
                priorFeedback.add("第 " + i + " 次结构不合法: " + detail);
                continue;
            }

            log.add("[iter " + i + "] PROGRAM VALIDATION PASSED");
            WeeklyReport draft = validation.report();

            EvaluatorFeedback feedback = evaluator.evaluate(stats, draft);
            evaluatorCalls++;
            if (feedback.pass()) {
                log.add("[iter " + i + "] EVALUATOR PASSED (score=" + feedback.score() + ", calls=" + evaluatorCalls + ")");
                return new LoopResult(draft, i, evaluatorCalls, List.copyOf(log));
            }
            String issueDetail = feedback.issues().isEmpty()
                    ? "无具体说明"
                    : String.join("; ", feedback.issues());
            log.add("[iter " + i + "] EVALUATOR REJECTED (score=" + feedback.score() + ") -> " + issueDetail);
            priorFeedback.add("第 " + i + " 次评估未通过(score=" + feedback.score() + "): " + issueDetail);
        }

        log.add("MAX_ITERATIONS(" + MAX_ITERATIONS + ") EXCEEDED：未产出合格周报");
        return new LoopResult(null, MAX_ITERATIONS, evaluatorCalls, List.copyOf(log));
    }
}