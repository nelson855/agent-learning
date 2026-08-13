package com.example.agentlearning.stage01;

/**
 * 按最大步数停止：模型步骤超过上限即强制结束，防止无限循环。
 */
public final class MaxStepsStopCondition implements StopCondition {

    public static final String MAX_STEPS_EXCEEDED = "AGENT_MAX_STEPS_EXCEEDED";

    private final int maxSteps;

    public MaxStepsStopCondition(int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps 必须大于 0，实际: " + maxSteps);
        }
        this.maxSteps = maxSteps;
    }

    @Override
    public String evaluate(int step) {
        return step >= maxSteps ? MAX_STEPS_EXCEEDED : null;
    }

    @Override
    public String description() {
        return "maxSteps=" + maxSteps;
    }
}
