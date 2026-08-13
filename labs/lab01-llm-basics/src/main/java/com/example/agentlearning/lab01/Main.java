package com.example.agentlearning.lab01;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * lab01-llm-basics 控制台聊天入口。
 *
 * <p>演示"LLM 无状态、多轮连续性由应用维护"：输入消息会追加到 history 再传给模型；
 * {@code /reset} 清空 history（模型立刻失忆）；{@code /history} 只打印消息数量与角色。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        LlmClient client = createClient();
        Conversation conversation = new Conversation(client);

        System.out.println("=== lab01-llm-basics: LLM 无状态演示 ===");
        System.out.println("输入消息回车发送；/reset 清空历史；/history 查看历史；/exit 退出。");
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
                if (input.equals("/reset")) {
                    conversation.reset();
                    System.out.println("[reset] history 已清空");
                    continue;
                }
                if (input.equals("/history")) {
                    System.out.println(conversation.historySummary());
                    continue;
                }
                LlmResponse response = conversation.sendUserMessage(input);
                System.out.println("assistant: " + response.content());
            }
        }
        System.out.println("bye");
    }

    /**
     * 配置来自环境变量或根目录 .env 文件（环境变量优先）；
     * 未配置时退回 {@link FakeLlmClient}，便于离线观察 history 行为。
     */
    private static LlmClient createClient() {
        String baseUrl = EnvFile.get("LLM_BASE_URL");
        String apiKey = EnvFile.get("LLM_API_KEY");
        String model = EnvFile.get("LLM_MODEL");
        if (isBlank(baseUrl) || isBlank(apiKey) || isBlank(model)) {
            System.out.println("[warn] 未找到 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL（环境变量或根目录 .env 文件），使用 FakeLlmClient（不调用真实模型）");
            return new FakeLlmClient();
        }
        return OpenAiCompatibleLlmClient.fromConfig();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
