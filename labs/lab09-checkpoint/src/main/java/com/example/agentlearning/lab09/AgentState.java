package com.example.agentlearning.lab09;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * Agent 运行时可恢复的状态（Checkpoint 保存的对象）。
 *
 * <p>这是 {@code state_json} 对应的结构，包含 Run 恢复所需的全部关键信息：
 * runId、goal、计划步骤列表（含各自状态）、当前执行到第几步、以及每步攒下来的重要结果。
 * 刻意<b>不保存</b>完整 Context 文本——需要时由 {@link ContextBuilder} 现组装。
 *
 * @param runId            运行标识（用于 {@code /resume runId}）
 * @param goal             本次任务目标
 * @param plan             有序的计划步骤
 * @param currentStepIndex 当前执行到第几步（下标）
 * @param stepResults      每步执行后的简短结果（按步骤 id 记录）
 */
public record AgentState(
        String runId,
        String goal,
        List<PlanStep> plan,
        int currentStepIndex,
        List<String> stepResults) {

    /** 下一个"还没执行完"的步骤；全部完成返回 -1。PENDING/RUNNING 都视为未完成。
     * 计算属性，不参与 Json 落库。 */
    @JsonIgnore
    public int nextPendingStepIndex() {
        List<PlanStep> steps = plan;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).status() != StepStatus.DONE) {
                return i;
            }
        }
        return -1;
    }

    @JsonIgnore
    public boolean isComplete() {
        return nextPendingStepIndex() == -1;
    }
}
