package com.example.agentlearning.stage01;

import java.util.List;

/**
 * 对照实验的固定 Workflow 运行结果。
 */
public record WorkflowResult(boolean success, String failureReason, int openCount, List<String> steps) {

    public static WorkflowResult success(int openCount, List<String> steps) {
        return new WorkflowResult(true, null, openCount, steps);
    }

    public static WorkflowResult failure(String failureReason, List<String> steps) {
        return new WorkflowResult(false, failureReason, 0, steps);
    }
}
