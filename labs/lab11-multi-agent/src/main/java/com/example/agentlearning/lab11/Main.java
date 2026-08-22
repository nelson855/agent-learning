package com.example.agentlearning.lab11;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * lab11 对照实验入口：Single Agent vs Multi-Agent (Orchestrator + 3 Workers)。
 *
 * <p>两种方案都用 FakeLlmClient 脚本驱动，不依赖真实在线模型。
 *
 * <p>观察：
 * <ol>
 *   <li>model_calls：Multi 是 Single 的 3 倍；</li>
 *   <li>context_chars：每个 Worker 上下文更聚焦（只看到自己需要的片段）；</li>
 *   <li>steps：Multi 有明确的细分步骤；</li>
 *   <li>success：两者都输出完整报告。</li>
 * </ol>
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        String dbFile = args.length > 0 ? args[0] : "data/lab11.db";
        var parent = Paths.get(dbFile).getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Database db = new Database("jdbc:sqlite:" + dbFile)) {
            TaskRepository repo = new TaskRepository(db);
            repo.seedDemoData();
            TaskStats stats = repo.aggregateStats();
            System.out.println("任务统计: " + ReportJson.toJson(stats));

            Comparison.run(stats);
        }
    }
}