package com.example.agentlearning.lab03;

import java.util.List;

/**
 * 一次 Agent 运行的结果：是否正常结束、最终回答、以及完整步骤轨迹。
 *
 * <p>{@code finished=false} 表示被停止条件（超时/超步数）拦停，
 * 此时 {@code answer} 为 {@code AGENT_MAX_STEPS_EXCEEDED}。
 */
public record AgentRun(boolean finished, String answer, List<StepTrace> steps) {

    public static AgentRun finalAnswer(String answer, List<StepTrace> steps) {
        return new AgentRun(true, answer, steps);
    }

    public static AgentRun maxStepsExceeded(List<StepTrace> steps) {
        return new AgentRun(false, AgentLoop.MAX_STEPS_EXCEEDED, steps);
    }
}
