package com.example.agentlearning.lab09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runner 的 Checkpoint / Resume 验收测试。
 *
 * <p>核心断言：崩溃后从最新 Checkpoint 恢复，已 DONE 的步骤<b>不重复执行</b>，
 * 只有未完成步骤被继续做掉。用 {@link AgentRunner#onStepDone} 回调对每一步精确计数，
 * 而非人肉看日志。
 */
class AgentRunnerTest {

    private Path dbFile;
    private Database db;
    private CheckpointRepository checkpoints;
    /** stepId -> 被完成的次数。 */
    private final ConcurrentHashMap<String, AtomicInteger> calls = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        dbFile = Files.createTempFile("lab09-runner-", ".db");
        db = new Database("jdbc:sqlite:" + dbFile);
        checkpoints = new CheckpointRepository(db);
        calls.clear();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private AgentRunner runner(CrashPolicy crashPolicy) {
        return new AgentRunner(new ToolRegistry().register("echo", ToolRegistry.echo()),
                checkpoints, new ContextBuilder(), crashPolicy,
                stepId -> calls.computeIfAbsent(stepId, k -> new AtomicInteger()).incrementAndGet());
    }

    private AgentState initialState(String runId) {
        return new AgentState(runId, "目标", AgentRunner.fiveStepPlan(), 0, List.of());
    }

    private int count(String stepId) {
        return calls.getOrDefault(stepId, new AtomicInteger()).get();
    }

    @Test
    void noCrashRunsAllFiveStepsOnce() {
        String runId = "run-ok";
        AgentState finalState = runner(CrashPolicy.NEVER).run(initialState(runId));

        assertTrue(finalState.isComplete());
        assertEquals(5, checkpoints.latestVersion(runId), "跑满 5 步应各有一次 checkpoint");
        for (int i = 1; i <= 5; i++) {
            assertEquals(1, count("s" + i), "步骤 s" + i + " 应恰好完成 1 次");
        }
    }

    @Test
    void resumeAfterCrashSkipsDoneSteps() {
        String runId = "run-crash";
        // 在第 3 步（index 2）前崩溃
        assertThrows(SimulatedCrashException.class,
                () -> runner(stepIndex -> stepIndex == 2).run(initialState(runId)));

        // 崩溃后：s1、s2 已 DONE 并落库，第 3 步未完成
        assertEquals(2, checkpoints.latestVersion(runId), "崩溃前应已保存 s1、s2 两个 checkpoint");
        AgentState afterCrash = checkpoints.loadLatest(runId).orElseThrow();
        assertEquals(2, afterCrash.nextPendingStepIndex(), "应恢复到第 3 步（下标 2）");

        // 重新启动：从最新 checkpoint 恢复，继续执行到完成
        AgentState restored = checkpoints.loadLatest(runId).orElseThrow();
        AgentState finalState = runner(CrashPolicy.NEVER).run(restored);

        assertTrue(finalState.isComplete());
        // s1、s2 只完成 1 次（崩溃前），s3~s5 各 1 次（恢复后）——DONE 步骤不重复执行
        for (int i = 1; i <= 5; i++) {
            assertEquals(1, count("s" + i), "步骤 s" + i + " 全程应恰好完成 1 次（含崩溃恢复）");
        }
        assertEquals(5, checkpoints.latestVersion(runId), "恢复后又保存了 s3、s4、s5");
    }

    @Test
    void resumeRebuildsRuntimeStateFromLastCheckpoint() {
        String runId = "run-rebuild";
        // 第 2 步（index 1）前崩溃
        assertThrows(SimulatedCrashException.class,
                () -> runner(stepIndex -> stepIndex == 1).run(initialState(runId)));

        AgentState afterCrash = checkpoints.loadLatest(runId).orElseThrow();
        assertEquals(1, afterCrash.nextPendingStepIndex(), "应恢复到第 2 步");
        assertEquals(StepStatus.DONE, afterCrash.plan().get(0).status(), "第 1 步保持 DONE");
        assertEquals(StepStatus.PENDING, afterCrash.plan().get(3).status(), "后续步骤仍为 PENDING");
    }
}
