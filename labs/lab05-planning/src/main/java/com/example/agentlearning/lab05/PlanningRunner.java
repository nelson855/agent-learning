package com.example.agentlearning.lab05;

/**
 * Plan-and-Replan 主循环：
 *
 * <pre>
 *   INITIAL PLAN → 逐步执行（STEP RESULT）
 *     → 全部成功？结束
 *     → 某步失败 → REPLAN REASON → Replanner → NEW PLAN → 继续执行
 *     → 超过 maxReplans → 剩余步骤 SKIPPED → 结束
 *   FINAL STATUS
 * </pre>
 *
 * <p>关键设计：<b>只在失败时 Replan</b>，不做无条件的每步重规划。
 */
public final class PlanningRunner {

    private final Planner planner;
    private final Replanner replanner;
    private final Executor executor;
    private final int maxReplans;

    public PlanningRunner(Planner planner, Replanner replanner, Executor executor, int maxReplans) {
        this.planner = planner;
        this.replanner = replanner;
        this.executor = executor;
        this.maxReplans = maxReplans;
    }

    public PlanRunResult run(String goal) {
        Plan plan = planner.createPlan(goal);
        printPlan("INITIAL PLAN", plan);

        int replans = 0;
        while (true) {
            PlanStep failed = executeRemaining(plan);
            if (failed == null) {
                break; // 全部 DONE
            }

            if (replans >= maxReplans) {
                System.out.println("REPLAN LIMIT REACHED: maxReplans=" + maxReplans
                        + "，剩余步骤标记 SKIPPED");
                for (PlanStep step : plan.steps()) {
                    if (step.status() == PlanStepStatus.PENDING) {
                        step.setStatus(PlanStepStatus.SKIPPED);
                    }
                }
                break;
            }

            System.out.println("REPLAN REASON: 步骤 [" + failed.id() + "] " + failed.description()
                    + " 执行失败: " + failed.failureReason());
            System.out.println();
            replans++;

            plan = replanner.replan(plan, failed);
            printPlan("NEW PLAN", plan);
        }

        PlanRunResult result = new PlanRunResult(plan, replans);
        System.out.println("FINAL STATUS: " + result.summary());
        return result;
    }

    /** 从前往后执行剩余 PENDING 步骤，返回第一个失败的步骤；全部成功返回 null。 */
    private PlanStep executeRemaining(Plan plan) {
        for (PlanStep step : plan.steps()) {
            if (step.status() != PlanStepStatus.PENDING) {
                continue;
            }
            StepOutcome outcome = executor.execute(step);
            System.out.println("STEP RESULT: [" + step.id() + "] " + step.description()
                    + " => " + step.status()
                    + (outcome.ok() ? "" : " (" + outcome.failureReason() + ")"));
            if (!outcome.ok()) {
                return step;
            }
        }
        return null;
    }

    private void printPlan(String title, Plan plan) {
        System.out.println(title);
        System.out.println("  goal: " + plan.goal());
        for (PlanStep step : plan.steps()) {
            System.out.println("  [" + step.id() + "] " + step.description()
                    + "  (" + step.status() + ")");
        }
        System.out.println();
    }
}
