package com.example.agentlearning.lab06;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Lab06 CLI 入口。
 *
 * <p>三个命令体系对应三个持久化维度：
 * <ul>
 *   <li>{@code /chat new}、{@code /chat <id>} —— 会话与对话历史（Conversation History）；</li>
 *   <li>{@code /state <runId>} —— Agent 运行状态（Agent State）；</li>
 *   <li>{@code /memory} —— 长期记忆（Long-term Memory）。</li>
 * </ul>
 * 除命令外，其他输入都会当作用户消息，先经过 Memory Extractor 判断是否保存，
 * 再交给 {@link StatefulAgentRunner} 持久化执行。
 *
 * <p>离线运行：{@code mvn compile exec:java -Dexec.args="--demo"} 会跑一遍
 * Session A（保存偏好）→"退出程序"→ Session B（检索偏好）的完整演示。
 */
public final class Main {

    private static final String DB_URL = "jdbc:sqlite:data/lab06.db";
    private static final String USER_ID = "demo-user";

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--demo".equals(args[0])) {
            runDemo();
        } else {
            runInteractive();
        }
    }

    // ---------------- Demo：Session A → 退出 → Session B ----------------

    private static void runDemo() throws Exception {
        System.out.println("==== Lab06 演示：Conversation History / Agent State / Long-term Memory ====");
        System.out.println();

        // 确定性剧本：extractor 与 agent 共用同一个脚本模型
        ScriptedLlmClient llm = ScriptedLlmClient.of(
                "{\"shouldRemember\":true,\"memoryType\":\"PREFERENCE\",\"content\":\"用户的 Java Demo 都使用 Maven\"}",
                "{\"type\":\"final\",\"answer\":\"好的，已记住：以后你的 Java Demo 都使用 Maven。\"}",
                "{\"shouldRemember\":false}",
                "{\"type\":\"final\",\"answer\":\"好的，我会用 Maven 来初始化你的 Java Demo。\"}");

        System.out.println("---- Session A：用户说出偏好，程序先存记忆，再回复 ----");
        Database db = openDatabase();
        Components a = build(db, llm);
        Conversation sessionA = a.conversations().create("Session A");
        String msgA = "以后我的 Java Demo 都使用 Maven。";
        System.out.println(">>> 用户: " + msgA);
        rememberAndRun(a, sessionA.id(), msgA);

        db.close();
        System.out.println();
        System.out.println("==== 进程退出（数据库已关闭，内存与对话历史全部丢弃） ====");
        System.out.println();

        System.out.println("---- Session B：\"重启\"后新会话，靠 memory 表回忆起偏好 ----");
        Database db2 = openDatabase();
        Components b = build(db2, llm);
        Conversation sessionB = b.conversations().create("Session B");
        String msgB = "帮我初始化一个 Java Demo。";
        System.out.println(">>> 用户: " + msgB);
        rememberAndRun(b, sessionB.id(), msgB);

        b.db().close();
        System.out.println();
        System.out.println("==== 演示结束：偏好跨会话保留在 memory 表（conversation 与 agent_run 是另两张表） ====");
    }

    private static void rememberAndRun(Components c, String conversationId, String userInput) {
        MemoryDecision decision = c.extractor().extract(userInput);
        if (decision.shouldRemember() && !decision.content().isBlank()) {
            Memory saved = c.memories().save(USER_ID, decision.memoryType(), decision.content(), 5);
            System.out.println("MEMORY SAVED: [" + saved.type() + "] " + saved.content());
        } else {
            System.out.println("MEMORY SKIPPED: 本条不需要长期保存");
        }
        System.out.println();
        c.runner().run(conversationId, userInput);
        System.out.println();
    }

    // ---------------- 交互模式 ----------------

    private static void runInteractive() throws Exception {
        Map<String, String> env = EnvFile.load();
        Database db = openDatabase();
        LlmClient llm;
        if (OpenAiCompatibleLlmClient.isConfigured(env)) {
            llm = OpenAiCompatibleLlmClient.fromEnv(env);
        } else {
            System.out.println("[警告] 未配置 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL，使用离线脚本模型（无法智能回复）。");
            System.out.println("      配置方式：环境变量，或仓库根目录 .env（见 .env.example）。\n");
            llm = ScriptedLlmClient.of(
                    "{\"shouldRemember\":false}",
                    "{\"type\":\"final\",\"answer\":\"（离线模式）未配置真实模型，本条无法智能回复，但你可以用 /state 观察状态持久化。\"}");
        }
        Components c = build(db, llm);

        Scanner scanner = new Scanner(System.in);
        String conversationId = null;
        printHelp();
        System.out.println();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("/exit")) {
                break;
            }
            if (line.equals("/help")) {
                printHelp();
                continue;
            }
            if (line.equals("/chat new")) {
                Conversation created = c.conversations().create("会话 " + (c.conversations().findAll().size() + 1));
                conversationId = created.id();
                System.out.println("已创建并切换到会话: " + created.id());
                continue;
            }
            if (line.startsWith("/chat ")) {
                String id = line.substring("/chat ".length()).trim();
                Optional<Conversation> found = c.conversations().findById(id);
                if (found.isPresent()) {
                    conversationId = id;
                    System.out.println("已切换到会话: " + id);
                } else {
                    System.out.println("未找到会话: " + id);
                }
                continue;
            }
            if (line.startsWith("/state ")) {
                String runId = line.substring("/state ".length()).trim();
                Optional<AgentRun> run = c.runs().findById(runId);
                if (run.isPresent()) {
                    printRun(run.get());
                } else {
                    System.out.println("未找到运行: " + runId);
                }
                continue;
            }
            if (line.equals("/memory")) {
                List<Memory> all = c.memories().findAll();
                if (all.isEmpty()) {
                    System.out.println("当前没有保存任何长期记忆");
                } else {
                    for (Memory m : all) {
                        System.out.println("- [" + m.type() + "] " + m.content() + " (importance=" + m.importance() + ")");
                    }
                }
                continue;
            }
            if (conversationId == null) {
                System.out.println("请先 /chat new 创建会话，或 /chat <id> 切换已有会话。");
                continue;
            }
            rememberAndRun(c, conversationId, line);
        }
        db.close();
        System.out.println("bye");
    }

    private static void printRun(AgentRun run) {
        System.out.println("run:      " + run.runId());
        System.out.println("conversation: " + run.conversationId());
        System.out.println("goal:     " + run.goal());
        System.out.println("status:   " + run.status());
        System.out.println("step:     " + run.currentStep());
        System.out.println("started:  " + run.startedAt());
        System.out.println("updated:  " + run.updatedAt());
    }

    private static void printHelp() {
        System.out.println("""
                命令：
                  /chat new         创建新会话并切换过去
                  /chat <id>        切换到已有会话（恢复对话历史）
                  /state <runId>    查看一次 Agent 运行的状态（goal/status/step）
                  /memory           查看已保存的长期记忆
                  /help             显示帮助
                  /exit             退出
                其他输入：作为用户消息运行 Agent（先提取记忆，再持久化执行）。
                演示：mvn compile exec:java -Dexec.args="--demo"
                """);
    }

    // ---------------- 装配 ----------------

    private record Components(
            Database db,
            ConversationRepository conversations,
            MessageRepository messages,
            AgentRunRepository runs,
            MemoryRepository memories,
            MemoryRetriever retriever,
            MemoryExtractor extractor,
            StatefulAgentRunner runner) {
    }

    private static Components build(Database db, LlmClient llm) {
        ConversationRepository conversations = new ConversationRepository(db);
        MessageRepository messages = new MessageRepository(db);
        AgentRunRepository runs = new AgentRunRepository(db);
        MemoryRepository memories = new MemoryRepository(db);
        MemoryRetriever retriever = new MemoryRetriever(memories);
        MemoryExtractor extractor = new MemoryExtractor(llm);
        TaskStore taskStore = new TaskStore(db);
        ToolRegistry tools = TaskTools.createDefault(taskStore);
        StatefulAgentRunner runner = new StatefulAgentRunner(llm, tools, messages, runs, retriever);
        return new Components(db, conversations, messages, runs, memories, retriever, extractor, runner);
    }

    private static Database openDatabase() throws Exception {
        Files.createDirectories(Path.of("data"));
        return new Database(DB_URL);
    }
}
