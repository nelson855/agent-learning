package com.example.agentlearning.lab02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * lab02-tool-calling 控制台入口。
 *
 * <p>演示"结构化输出 + 单次工具调用"：输入自然语言，模型输出一个结构化 JSON，
 * 程序解析出工具调用并执行，把结果展示给你。不做多轮循环。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        TaskStore store = createTaskStore();
        ToolRegistry registry = DemoTools.createDefault(store);
        ToolRunner runner = new ToolRunner(createLlmClient(), registry);

        System.out.println("=== lab02-tool-calling: Structured Output + Tool Calling ===");
        System.out.println("输入自然语言回车发送；/tools 查看工具说明；/exit 退出。");
        System.out.println("示例：帮我创建一个任务：写周报 / 计算 (1+2)*3");
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
                if (input.equals("/tools")) {
                    System.out.println(registry.toolsInstruction());
                    continue;
                }
                ToolResult result = runner.run(input);
                System.out.println((result.success() ? "→ " : "✗ ") + result.message());
            }
        }
        store.close();
        System.out.println("bye");
    }

    private static TaskStore createTaskStore() throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);
        Path dbFile = dataDir.resolve("lab02.db");
        System.out.println("[db] SQLite 数据文件: " + dbFile.toAbsolutePath());
        return new TaskStore("jdbc:sqlite:" + dbFile);
    }

    /**
     * 配置来自环境变量或根目录 .env 文件（环境变量优先）；
     * 未配置时退回 {@link FakeLlmClient}，便于离线演示程序侧的工具分派能力。
     */
    private static LlmClient createLlmClient() {
        String baseUrl = EnvFile.get("LLM_BASE_URL");
        String apiKey = EnvFile.get("LLM_API_KEY");
        String model = EnvFile.get("LLM_MODEL");
        if (isBlank(baseUrl) || isBlank(apiKey) || isBlank(model)) {
            System.out.println("[warn] 未找到 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL（环境变量或根目录 .env），使用 FakeLlmClient（不调用真实模型）");
            return new FakeLlmClient("{\"tool\":null,\"text\":\"(离线演示) 未配置真实模型，我不会真的执行工具。请在根目录 .env 填入 LLM_API_KEY 后观察模型如何选择工具。\"}");
        }
        return OpenAiCompatibleLlmClient.fromConfig();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
