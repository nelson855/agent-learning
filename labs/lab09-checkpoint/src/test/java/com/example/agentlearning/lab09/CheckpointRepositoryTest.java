package com.example.agentlearning.lab09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checkpoint 存取验收测试：验证"每次保存新版本不覆盖"与"能读回最新状态"。
 */
class CheckpointRepositoryTest {

    private Path dbFile;
    private Database db;
    private CheckpointRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = Files.createTempFile("lab09-checkpoint-repo-", ".db");
        db = new Database("jdbc:sqlite:" + dbFile);
        repository = new CheckpointRepository(db);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private AgentState state(String runId) {
        return new AgentState(runId, "目标", AgentRunner.fiveStepPlan(), 0, List.of());
    }

    @Test
    void latestVersionStartsAtZeroAndIncrements() {
        String runId = "run-a";
        assertEquals(0, repository.latestVersion(runId), "未保存时最新版本应为 0");

        repository.save(state(runId));
        assertEquals(1, repository.latestVersion(runId));
        assertEquals(1, repository.countVersions(runId));

        repository.save(state(runId));
        repository.save(state(runId));
        assertEquals(3, repository.latestVersion(runId));
        assertEquals(3, repository.countVersions(runId), "每次保存新增一行，不覆盖历史");
    }

    @Test
    void versionsArePerRunId() {
        repository.save(state("run-a"));
        repository.save(state("run-a"));
        repository.save(state("run-b"));

        assertEquals(2, repository.latestVersion("run-a"));
        assertEquals(1, repository.latestVersion("run-b"));
    }

    @Test
    void loadLatestReturnsEmptyForUnknownRun() {
        assertTrue(repository.loadLatest("no-such-run").isEmpty());
    }

    @Test
    void stateRoundTripsThroughJson() {
        String runId = "run-d";
        AgentState original = new AgentState(runId, "目标", AgentRunner.fiveStepPlan(), 0, List.of());
        repository.save(original);

        AgentState loaded = repository.loadLatest(runId).orElseThrow();
        assertEquals(original.runId(), loaded.runId());
        assertEquals(original.goal(), loaded.goal());
        assertEquals(original.plan(), loaded.plan());
        assertEquals(original.stepResults(), loaded.stepResults());
    }
}
