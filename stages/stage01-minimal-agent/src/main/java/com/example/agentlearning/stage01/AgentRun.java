package com.example.agentlearning.stage01;

import java.util.List;

/**
 * 一次 Agent 运行的结果：是否正常结束、最终回答、步骤轨迹。
 */
public record AgentRun(boolean finished, String answer, List<StepTrace> steps) {

    public static AgentRun finalAnswer(String answer, List<StepTrace> steps) {
        return new AgentRun(true, answer, steps);
    }

    public static AgentRun stopped(String reason, List<StepTrace> steps) {
        return new AgentRun(false, reason, steps);
    }
}
