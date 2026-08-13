package com.example.agentlearning.stage01;

/**
 * Agent 停止条件：决定循环"什么时候必须停下"。
 *
 * <p>本阶段只有一种实现 {@link MaxStepsStopCondition}，但把它抽成接口，
 * 让"停止逻辑"和"循环主体"解耦——以后加超时、加校验上限都不用改 {@link AgentRunner}。
 */
public interface StopCondition {

    /**
     * 在每步之后被调用。
     *
     * @param step 当前已经完成的步数（从 1 开始）
     * @return null 表示继续循环；非 null 表示命中停止条件，返回值为停止原因
     */
    String evaluate(int step);

    /** 一段可展示的描述，用于 CLI 打印停止原因。 */
    String description();
}
