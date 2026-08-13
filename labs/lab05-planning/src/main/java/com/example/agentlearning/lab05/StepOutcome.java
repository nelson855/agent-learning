package com.example.agentlearning.lab05;

/**
 * 一个计划步骤的执行结果。
 */
public record StepOutcome(boolean ok, String failureReason, String message) {

    public static StepOutcome ok(String message) {
        return new StepOutcome(true, null, message);
    }

    public static StepOutcome fail(String failureReason) {
        return new StepOutcome(false, failureReason, null);
    }
}
