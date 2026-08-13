package com.example.agentlearning.lab05;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟"完成一个学习步骤"的 Fake 工具：{@code completeLearningStep(stepId)}。
 *
 * <p>默认行为：<b>步骤 S2 第一次执行固定失败</b>，返回 {@link #DEPENDENCY_MISSING}；
 * 之后对同一个 stepId 的调用成功。
 *
 * <p>可通过 {@link #failOnFirst(String, int)} 配置任意步骤"前 N 次失败"，
 * 用于构造"反复失败"来观察 Replan 上限。
 */
public final class FakeLearningStepTool {

    public static final String DEPENDENCY_MISSING = "DEPENDENCY_MISSING";

    private final Map<String, Integer> failFirstCount = new HashMap<>();
    private final Map<String, Integer> attempts = new HashMap<>();

    public FakeLearningStepTool() {
        failOnFirst("S2", 1);
    }

    /** 让某个 stepId 的前 n 次调用都失败（n=0 表示永不失败）。 */
    public FakeLearningStepTool failOnFirst(String stepId, int n) {
        failFirstCount.put(stepId, n);
        return this;
    }

    public StepOutcome complete(String stepId) {
        int attempt = attempts.merge(stepId, 1, Integer::sum);
        int failN = failFirstCount.getOrDefault(stepId, 0);
        if (attempt <= failN) {
            return StepOutcome.fail(DEPENDENCY_MISSING + "（第 " + attempt + " 次尝试）");
        }
        return StepOutcome.ok("步骤 " + stepId + " 执行完成");
    }
}
