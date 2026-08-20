package com.example.agentlearning.lab09;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 计划步骤的状态机。
 *
 * <pre>
 * PENDING → RUNNING → DONE
 * </pre>
 *
 * Checkpoint 的意义在于：崩溃时 RUNNING/PENDING 的步骤要重做，
 * 而 DONE 的步骤绝不允许重复执行。
 */
public enum StepStatus {
    PENDING("pending"),
    RUNNING("running"),
    DONE("done");

    private final String wireName;

    StepStatus(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }
}
