package com.example.agentlearning.lab08;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Lab08 入口：观察"长对话 → 结构化压缩 → 上下文变小但信息可恢复"。
 *
 * <p>{@code --demo}：用脚本模型离线演示——生成 40 轮假对话（80 条消息），
 * 触发 6 次压缩，最后展示 RAW_HISTORY_COUNT（总产量）对比
 * RECENT_COUNT（上下文实际保留）与 FINAL_CONTEXT_COUNT（一次调用的消息数）。
 *
 * <p>不带参数：交互模式，配了真实模型则走真实 LLM，否则用脚本兜底。
 */
public final class Main {

    private static final String SYSTEM_PROMPT =
            "你是一个任务型 Agent。基于摘要和最近对话继续推进任务，回答尽量简洁。";

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--demo".equals(args[0])) {
            runDemo();
        } else {
            runInteractive();
        }
    }

    // ------------------------------------------------------------------
    // 离线演示
    // ------------------------------------------------------------------

    private static void runDemo() {
        Database db = new Database("jdbc:sqlite:data/lab08-context-compaction.db");
        clear(db);

        MessageRepository messages = new MessageRepository(db);
        ConversationSummaryRepository summaries = new ConversationSummaryRepository(db);
        ScriptedLlmClient summarizerLlm = ScriptedLlmClient.of(SUMMARY_V1, SUMMARY_V2, SUMMARY_V3);
        ConversationSummarizer summarizer = new ConversationSummarizer(summarizerLlm);
        CompactionService service = new CompactionService(
                messages, summaries, summarizer, new ContextPolicy(), new ContextBuilder(), SYSTEM_PROMPT);

        String conversationId = "c-demo";
        System.out.println("==== Lab08 演示：Context Compaction ====");
        System.out.println("策略：消息超过 " + ContextPolicy.DEFAULT_COMPACT_AFTER
                + " 条时压缩，保留最近 " + ContextPolicy.DEFAULT_RECENT_MESSAGES + " 条");
        System.out.println("生成 38 轮假对话（每轮 user + assistant，共 76 条）...\n");
        FakeConversationGenerator.generate(service, conversationId, 38);

        ConversationSummary latest = service.latestSummary(conversationId);
        System.out.println("\n--- 压缩统计 ---");
        System.out.println("RAW_HISTORY_COUNT（本轮累计产生的原始消息数）: " + service.rawHistoryCount());
        System.out.println("SUMMARY_VERSION（最新摘要版本）: " + (latest == null ? 0 : latest.version()));
        System.out.println("RECENT_COUNT（message 表实际保留的消息数）: " + messages.countByConversation(conversationId));

        System.out.println("\n--- 最新摘要内容（压缩产物，不随消息删除丢失）---");
        System.out.println(ContextBuilder.renderSummary(latest));

        System.out.println("\n--- 一次 buildContext 组装出的上下文（每次 build 都会打印这三行）---");
        List<Message> context = service.buildContext(conversationId, "请继续推进任务");
        System.out.println("FINAL_CONTEXT_COUNT 以上一次打印为准：共 " + context.size() + " 条消息");

        db.close();
        System.out.println("\n结论：产出了 76 条原始消息，但上下文里只保留最近的 10 条 + 1 份结构化摘要。"
                + "未完成事项、决定、事实都压缩进了 Summary，不会因删除旧消息而丢失。"
                + "若重启进程，Summary 仍能从 conversation_summary 表读回。");
    }

    // ------------------------------------------------------------------
    // 交互模式
    // ------------------------------------------------------------------

    private static void runInteractive() {
        Database db = new Database("jdbc:sqlite:data/lab08-context-compaction.db");
        Map<String, String> env = EnvFile.load();
        boolean online = OpenAiCompatibleLlmClient.isConfigured(env);

        MessageRepository messages = new MessageRepository(db);
        ConversationSummaryRepository summaries = new ConversationSummaryRepository(db);
        LlmClient chatLlm = online
                ? OpenAiCompatibleLlmClient.fromEnv(env)
                : ScriptedLlmClient.of("收到，继续推进。");
        LlmClient summarizerLlm = online
                ? OpenAiCompatibleLlmClient.fromEnv(env)
                : ScriptedLlmClient.of(SUMMARY_V1, SUMMARY_V2, SUMMARY_V3);
        CompactionService service = new CompactionService(
                messages, summaries, new ConversationSummarizer(summarizerLlm),
                new ContextPolicy(), new ContextBuilder(), SYSTEM_PROMPT);

        String conversationId = "c-interactive-" + System.currentTimeMillis();
        System.out.println("==== Lab08 交互模式 ====" + (online ? "（真实模型）" : "（脚本兜底）"));
        System.out.println("消息超过 " + ContextPolicy.DEFAULT_COMPACT_AFTER + " 条后自动压缩。输入 /summary 查看摘要，/exit 退出。");
        System.out.println("会话 ID: " + conversationId);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nyou> ");
            String input = scanner.nextLine();
            if (input.isBlank()) {
                continue;
            }
            if ("/exit".equalsIgnoreCase(input.trim())) {
                break;
            }
            if ("/summary".equalsIgnoreCase(input.trim())) {
                ConversationSummary summary = service.latestSummary(conversationId);
                if (summary == null) {
                    System.out.println("（尚未触发压缩，还没有摘要）");
                } else {
                    System.out.println(ContextBuilder.renderSummary(summary));
                }
                continue;
            }
            service.appendUser(conversationId, input);
            List<Message> context = service.buildContext(conversationId, input);
            String reply = chatLlm.chat(context).content();
            service.appendAssistant(conversationId, reply);
            System.out.println("agent> " + reply);
        }
        db.close();
        System.out.println("再见。");
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    private static void clear(Database db) {
        try (Statement st = db.connection().createStatement()) {
            st.executeUpdate("DELETE FROM message");
            st.executeUpdate("DELETE FROM conversation_summary");
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("清理演示数据失败", e);
        }
    }

    private static final String SUMMARY_V1 = """
            {
              "goal": "搭建任务系统",
              "completed": ["创建任务模块", "确定技术选型"],
              "importantFacts": ["用户偏好 Maven 构建", "数据库用 SQLite"],
              "decisions": ["不用框架，走 JDBC", "任务按优先级排序"],
              "openQuestions": ["是否需要 Web 界面"],
              "pendingActions": ["实现任务列表查询"]
            }""";

    private static final String SUMMARY_V2 = """
            {
              "goal": "搭建并完善任务系统",
              "completed": ["实现任务列表查询", "支持按状态过滤"],
              "importantFacts": ["用户偏好 Maven 构建", "数据库用 SQLite"],
              "decisions": ["任务状态：待办/进行中/完成"],
              "openQuestions": [],
              "pendingActions": ["实现任务编辑"]
            }""";

    private static final String SUMMARY_V3 = """
            {
              "goal": "搭建并完善任务系统",
              "completed": ["实现任务编辑", "补充单元测试"],
              "importantFacts": ["用户偏好 Maven 构建", "数据库用 SQLite"],
              "decisions": ["CLI 作为主要入口"],
              "openQuestions": ["何时引入记忆"],
              "pendingActions": ["接入检索增强生成"]
            }""";
}
