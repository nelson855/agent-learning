package com.example.agentlearning.lab09;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lab09 入口：观察 "多步任务执行到一半崩溃 → 从 Checkpoint 恢复，跳过已完成的步骤"。
 *
 * <p>{@code --demo}：离线演示——构造 5 步任务，允许在第 3 步前模拟崩溃，
 * 再新建 Runner 从最新 Checkpoint 恢复并执行到完成，打印验收输出。
 *
 * <p>不带参数：交互模式——可输入指令推进任务到任意步，输入 {@code /crash} 模拟崩溃退出；
 * 再次启动后输入 {@code /resume runId} 从上次 checkpoint 继续（DONE 步骤不会重做）。
 */
public final class Main {

    private static final AtomicLong RUN_COUNTER = new AtomicLong();
    private static final String DB_URL = "jdbc:sqlite:data/lab09-checkpoint.db";

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--demo".equals(args[0])) {
            runDemo();
        } else {
            runInteractive();
        }
    }

    // ------------------------------------------------------------------
    // 离线演示
    // ------------------------------------------------------------------

    private static void runDemo() throws Exception {
        Path dbFile = Files.createTempFile("lab09-demo-", ".db");
        Database db = new Database("jdbc:sqlite:" + dbFile);
        CheckpointRepository checkpoints = new CheckpointRepository(db);

        String runId = newRunId();
        System.out.println("==== Lab09 演示：Checkpoint + Resume ====");
        System.out.println("构造 5 步任务 runId=" + runId + "，允许在第 3 步前 /crash\n");

        // 第一次运行：在第 3 步（index 2）执行前崩溃
        AgentState initial = new AgentState(runId, "搭建带可恢复性的任务 Agent",
                AgentRunner.fiveStepPlan(), 0, List.of());
        AgentRunner crashRunner = new AgentRunner(buildTools(), checkpoints,
                new ContextBuilder(), stepIndex -> stepIndex == 2);

        try {
            crashRunner.run(initial);
        } catch (SimulatedCrashException e) {
            System.out.println("SIMULATED CRASH");
            System.out.println("→ " + e.getMessage() + "\n");
        }
        System.out.println("（进程此刻已中断；再次启动后加载最新 Checkpoint 恢复）\n");

        // 重新启动：加载最新 Checkpoint，从下一待办步骤恢复
        AgentState restored = checkpoints.loadLatest(runId).orElseThrow();
        int resumeFrom = restored.nextPendingStepIndex();
        System.out.println("LOADED CHECKPOINT version=" + checkpoints.latestVersion(runId));
        System.out.println("RESUME FROM step=" + (resumeFrom + 1) + " (" + restored.plan().get(resumeFrom).id() + ")");
        System.out.println("—— 已 DONE 的步骤 " + doneIds(restored) + " 将不会重复执行\n");

        System.out.println("---- 恢复上下文（ContextBuilder 现组装）----");
        AgentRunner resumeRunner = new AgentRunner(buildTools(), checkpoints,
                new ContextBuilder(), CrashPolicy.NEVER);
        System.out.println(resumeRunner.renderContext(restored));

        System.out.println("---- 继续执行到完成 ----");
        AgentState finalState = resumeRunner.run(restored);
        System.out.println("\n完成。全部 " + finalState.plan().size() + " 步 DONE，共保存检查点 "
                + checkpoints.countVersions(runId) + " 个版本。");

        db.close();
    }

    // ------------------------------------------------------------------
    // 交互模式
    // ------------------------------------------------------------------

    private static void runInteractive() throws Exception {
        Database db = new Database(DB_URL);
        CheckpointRepository checkpoints = new CheckpointRepository(db);
        Scanner scanner = new Scanner(System.in);

        System.out.println("==== Lab09 交互模式：Checkpoint + Resume ====");
        System.out.println("输入 /start 创建一个 5 步任务（自动执行并在第 3 步前模拟崩溃）；");
        System.out.println("记录输出的 RUN_ID（或本模块 data 目录里的数据），重新启动本程序，");
        System.out.println("再输入 /resume runId 从最新 Checkpoint 继续（DONE 步骤不重做）；/exit 退出。\n");

        while (true) {
            System.out.print("cmd> ");
            String line = scanner.nextLine().trim();
            if (line.isBlank()) {
                continue;
            }
            if ("/exit".equalsIgnoreCase(line)) {
                break;
            }
            if (line.startsWith("/resume")) {
                String runId = line.substring("/resume".length()).trim();
                resumeInteractive(db, checkpoints, runId);
                continue;
            }
            if ("/start".equalsIgnoreCase(line)) {
                startInteractive(db, checkpoints);
                continue;
            }
            System.out.println("未知命令。可用: /start /resume <runId> /exit");
        }
        db.close();
        System.out.println("再见。");
    }

    /** 交互：创建新任务并立刻推进（统一在第 3 步崩溃，演示 checkpoint 已落库）。 */
    private static void startInteractive(Database db, CheckpointRepository checkpoints) {
        String runId = newRunId();
        AgentState state = new AgentState(runId, "搭建带可恢复性的任务 Agent",
                AgentRunner.fiveStepPlan(), 0, List.of());
        // 交互演示统一在第 3 步（index 2）前崩溃，便于观察恢复
        AgentRunner runner = new AgentRunner(buildTools(), checkpoints, new ContextBuilder(),
                stepIndex -> stepIndex == 2);
        try {
            runner.run(state);
            System.out.println("（任务已全部完成）");
        } catch (SimulatedCrashException e) {
            System.out.println("SIMULATED CRASH");
            System.out.println("→ " + e.getMessage());
            System.out.println("RUN_ID=" + runId + "（重新启动本程序后 /resume " + runId + " 恢复）");
        }
    }

    /** 交互：加载某个 runId 的最新 Checkpoint 并恢复执行到完成。 */
    private static void resumeInteractive(Database db, CheckpointRepository checkpoints, String runId) {
        AgentState restored = checkpoints.loadLatest(runId).orElse(null);
        if (restored == null) {
            System.out.println("未找到 runId=" + runId + " 的 Checkpoint。");
            return;
        }
        int resumeFrom = restored.nextPendingStepIndex();
        System.out.println("LOADED CHECKPOINT version=" + checkpoints.latestVersion(runId));
        System.out.println("RESUME FROM step=" + (resumeFrom + 1));
        AgentRunner runner = new AgentRunner(buildTools(), checkpoints, new ContextBuilder(), CrashPolicy.NEVER);
        AgentState finalState;
        try {
            finalState = runner.run(restored);
        } catch (SimulatedCrashException e) {
            System.out.println("SIMULATED CRASH → " + e.getMessage());
            System.out.println("可再次 /resume " + runId);
            return;
        }
        System.out.println("完成。全部 " + finalState.plan().size() + " 步 DONE。");
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    private static ToolRegistry buildTools() {
        return new ToolRegistry()
                .register("echo", ToolRegistry.echo());
    }

    private static String newRunId() {
        return "run-" + System.currentTimeMillis() + "-" + RUN_COUNTER.incrementAndGet();
    }

    private static String doneIds(AgentState state) {
        return state.plan().stream()
                .filter(s -> s.status() == StepStatus.DONE)
                .map(PlanStep::id)
                .toList()
                .toString();
    }
}
