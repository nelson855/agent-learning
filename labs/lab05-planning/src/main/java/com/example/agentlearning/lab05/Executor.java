package com.example.agentlearning.lab05;

/**
 * 计划执行器：把一个 {@link PlanStep} 推到 {@code RUNNING}，调用工具执行，
 * 根据结果置 {@code DONE} 或 {@code FAILED}（记录失败原因）。
 */
public final class Executor {

    private final FakeLearningStepTool tool;

    public Executor(FakeLearningStepTool tool) {
        this.tool = tool;
    }

    public StepOutcome execute(PlanStep step) {
        step.setStatus(PlanStepStatus.RUNNING);
        StepOutcome outcome = tool.complete(step.id());
        if (outcome.ok()) {
            step.setStatus(PlanStepStatus.DONE);
        } else {
            step.setStatus(PlanStepStatus.FAILED);
            step.setFailureReason(outcome.failureReason());
        }
        return outcome;
    }
}
