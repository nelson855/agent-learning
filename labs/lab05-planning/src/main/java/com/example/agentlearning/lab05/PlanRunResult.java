package com.example.agentlearning.lab05;

/**
 * 一次 Plan-and-Replan 运行的最终结果。
 */
public record PlanRunResult(Plan plan, int replans) {

    public boolean allDone() {
        return plan.steps().stream().allMatch(step -> step.status() == PlanStepStatus.DONE);
    }

    /** 一段可展示的最终状态摘要，用于 CLI 打印 FINAL STATUS。 */
    public String summary() {
        long done = plan.steps().stream().filter(s -> s.status() == PlanStepStatus.DONE).count();
        long skipped = plan.steps().stream().filter(s -> s.status() == PlanStepStatus.SKIPPED).count();
        long total = plan.steps().size();
        if (allDone()) {
            return "SUCCESS（" + done + "/" + total + " DONE, replans=" + replans + "）";
        }
        return "INCOMPLETE（" + done + "/" + total + " DONE, " + skipped
                + " SKIPPED, replans=" + replans + "）";
    }
}
