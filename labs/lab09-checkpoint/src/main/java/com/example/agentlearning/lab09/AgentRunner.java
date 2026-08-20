package com.example.agentlearning.lab09;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 驱动多步任务执行的 Agent 循环 + Checkpoint 落点。
 *
 * <p>核心循环（每执行一步）：</p>
 * <pre>
 * step = NEXT_PENDING
 *   → mark RUNNING
 *   → 若崩溃策略命中：抛 SimulatedCrashException（进程中断）
 *   → 调用 planStep.tool 对应的工具
 *   → mark DONE、记录结果
 *   → {@code checkpointRepository.save(state)}   打印 CHECKPOINT SAVED
 * </pre>
 *
 * <p>{@code resume} 时从外部加载最新 Checkpoint 得到 state，再交给
 * {@link #run(AgentState)}，它从 {@code nextPendingStepIndex()} 继续——
 * DONE 的步骤天然不会重复执行。
 */
public final class AgentRunner {

    private final ToolRegistry tools;
    private final CheckpointRepository checkpoints;
    private final ContextBuilder contextBuilder;
    private final CrashPolicy crashPolicy;
    /** 每完成一步的回调（step.id）。供观察与测试断言"哪一步被执行了几次"。 */
    private final Consumer<String> onStepDone;

    public AgentRunner(ToolRegistry tools, CheckpointRepository checkpoints,
            ContextBuilder contextBuilder, CrashPolicy crashPolicy) {
        this(tools, checkpoints, contextBuilder, crashPolicy, stepId -> { });
    }

    public AgentRunner(ToolRegistry tools, CheckpointRepository checkpoints,
            ContextBuilder contextBuilder, CrashPolicy crashPolicy, Consumer<String> onStepDone) {
        this.tools = tools;
        this.checkpoints = checkpoints;
        this.contextBuilder = contextBuilder;
        this.crashPolicy = crashPolicy;
        this.onStepDone = onStepDone;
    }

    /** 从 state 的下一待办步骤开始执行，直到全部完成或崩溃；返回最终 state。 */
    public AgentState run(AgentState state) {
        AgentState current = state;
        while (!current.isComplete()) {
            current = oneStep(current);
        }
        return current;
    }

    private AgentState oneStep(AgentState state) {
        int index = state.nextPendingStepIndex();
        String runId = state.runId();

        if (crashPolicy.shouldCrashBefore(index)) {
            throw new SimulatedCrashException(
                    "SIMULATED CRASH before step " + (index + 1) + ": " + state.plan().get(index).id());
        }

        List<PlanStep> plan = new ArrayList<>(state.plan());
        PlanStep step = plan.get(index);

        // 标记 RUNNING（不落 checkpoint：RUNNING 代表"正在做但没做完"，崩溃后仍按未完成恢复）
        plan.set(index, step.withStatus(StepStatus.RUNNING));

        // 执行工具
        Tool tool = tools.get(step.tool());
        String result = tool.execute(step.args());
        System.out.println("[STEP " + (index + 1) + "] " + step.id() + " → " + result);

        // 标记 DONE 并记录结果
        plan.set(index, step.withStatus(StepStatus.DONE));
        List<String> results = new ArrayList<>(state.stepResults());
        results.add(step.id() + ": " + result);
        onStepDone.accept(step.id());

        AgentState next = new AgentState(runId, state.goal(), List.copyOf(plan), index, List.copyOf(results));

        // 每次推进都保存一个新 version（不覆盖历史）
        checkpoints.save(next);
        System.out.println("CHECKPOINT SAVED version=" + checkpoints.latestVersion(runId));
        return next;
    }

    /** 构造一段计划：5 步任务，全部走确定性 echo 工具。 */
    public static List<PlanStep> fiveStepPlan() {
        return List.of(
                new PlanStep("s1", "收集需求", "echo", "需求: 搭建任务系统", StepStatus.PENDING),
                new PlanStep("s2", "设计模块", "echo", "设计: agent_checkpoint 表", StepStatus.PENDING),
                new PlanStep("s3", "实现存储", "echo", "实现: CheckpointRepository", StepStatus.PENDING),
                new PlanStep("s4", "实现循环", "echo", "实现: AgentRunner", StepStatus.PENDING),
                new PlanStep("s5", "验证恢复", "echo", "验证: resume 后跳过 DONE", StepStatus.PENDING));
    }

    /** 供 {@code /resume} 打印恢复上下文。 */
    public String renderContext(AgentState state) {
        return contextBuilder.build(state);
    }
}
