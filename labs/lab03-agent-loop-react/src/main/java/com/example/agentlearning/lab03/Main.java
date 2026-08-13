package com.example.agentlearning.lab03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * lab03-agent-loop-react 控制台入口。
 *
 * <p>演示 Agent Loop：输入一个目标，Agent 会反复"决策 → 执行工具 → 观察结果"
 * 直到给出最终回答，或超过 {@code maxSteps} 被强制停止。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        TaskStore store = createTaskStore();
        ToolRegistry registry = DemoTools.createDefault(store);
        AgentLoop loop = new AgentLoop(createLlmClient(), registry);

        System.out.println("=== lab03-agent-loop-react: Agent Loop (ReAct) ===");
        System.out.println("输入目标回车，Agent 会多步执行直到给出最终回答；/exit 退出。");
        System.out.println("Demo 目标：创建一个“学习 Agent Loop”的任务，然后再次查询它，最后告诉我任务 ID 与状态");
        System.out.println();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String input = line.trim();
                if (input.isBlank()) {
                    continue;
                }
                if (input.equals("/exit")) {
                    break;
                }
                System.out.println(">>> " + input);
                AgentRun run = loop.run(input);
                System.out.println("=== 运行结束 ===");
                if (run.finished()) {
                    System.out.println("FINAL ANSWER: " + run.answer());
                } else {
                    System.out.println("未完成（" + loop.maxSteps() + " 步内未给出 final）: " + run.answer());
                }
                System.out.println();
            }
        }
        store.close();
        System.out.println("bye");
    }

    private static TaskStore createTaskStore() throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);
        Path dbFile = dataDir.resolve("lab03.db");
        System.out.println("[db] SQLite 数据文件: " + dbFile.toAbsolutePath());
        return new TaskStore("jdbc:sqlite:" + dbFile);
    }

    /**
     * 配置来自环境变量或根目录 .env 文件（环境变量优先）；
     * 未配置时退回 {@link ScriptedLlmClient}（立即给 final），便于离线演示 Loop 外壳。
     */
    private static LlmClient createLlmClient() {
        String baseUrl = EnvFile.get("LLM_BASE_URL");
        String apiKey = EnvFile.get("LLM_API_KEY");
        String model = EnvFile.get("LLM_MODEL");
        if (isBlank(baseUrl) || isBlank(apiKey) || isBlank(model)) {
            System.out.println("[warn] 未找到 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL（环境变量或根目录 .env），使用 ScriptedLlmClient（不调用真实模型）");
            return new ScriptedLlmClient("{\"type\":\"final\",\"answer\":\"(离线演示) 未配置真实模型，Agent 直接给出最终回答。请在根目录 .env 填入 LLM_API_KEY 后观察真实的多步决策。\"}");
        }
        return OpenAiCompatibleLlmClient.fromConfig();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
