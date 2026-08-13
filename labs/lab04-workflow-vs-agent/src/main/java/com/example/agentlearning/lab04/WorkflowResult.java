package com.example.agentlearning.lab04;

import java.util.List;

/**
 * Version A（固定 Workflow）的一次运行结果。
 *
 * <p>{@code steps} 记录了实际执行到的步骤名，便于观察"路径由程序固定"以及失败发生在哪一步。
 */
public record WorkflowResult(boolean success, String failureReason, Task task, List<String> steps) {

    public static WorkflowResult success(Task task, List<String> steps) {
        return new WorkflowResult(true, null, task, steps);
    }

    public static WorkflowResult failure(String failureReason, List<String> steps) {
        return new WorkflowResult(false, failureReason, null, steps);
    }
}
