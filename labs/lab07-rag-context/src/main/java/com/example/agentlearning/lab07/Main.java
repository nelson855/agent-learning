package com.example.agentlearning.lab07;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

/**
 * Lab07 CLI 入口。
 *
 * <p>演示用两个对照问题证明 Memory 与 RAG 是<b>两条不同的检索路径</b>：
 * <ul>
 *   <li>「任务系统使用什么数据库？」—— 答案来自 RAG（knowledge_doc 表，项目规范）；</li>
 *   <li>「我的项目用什么构建？」—— 答案来自 Memory（memory 表，用户偏好）。</li>
 * </ul>
 * 每次问答都会打印 MEMORY RETRIEVAL / RAG RETRIEVAL / CONTEXT SUMMARY，观察 Context 如何组装。
 *
 * <p>离线演示：{@code mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab07.Main -Dexec.args="--demo"}
 */
public final class Main {

    private static final String DB_URL = "jdbc:sqlite:data/lab07.db";
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

    // ---------------- Demo ----------------

    private static void runDemo() throws Exception {
        System.out.println("==== Lab07 演示：Memory / RAG / Context 的区别 ====");
        System.out.println();

        ScriptedLlmClient llm = ScriptedLlmClient.of(
                "{\"shouldRemember\":true,\"memoryType\":\"PREFERENCE\",\"content\":\"用户偏好 Maven 构建\"}",
                "根据项目文档，任务系统使用 SQLite 数据库。",
                "根据你的长期偏好，你偏好 Maven 构建。",
                "根据编码规范，本项目使用 Maven 作为构建工具。");

        Database db = openDatabase();
        Components c = build(db, llm);
        printImport(c);

        // 先让用户说出偏好，走一遍 Memory Extractor 保存流程
        String preference = "以后我的 Java 项目都用 Maven 构建。";
        System.out.println(">>> 用户: " + preference);
        MemoryDecision decision = c.extractor().extract(preference);
        if (decision.shouldRemember() && !decision.content().isBlank()) {
            c.memories().save(USER_ID, decision.memoryType(), decision.content(), 5);
            System.out.println("MEMORY SAVED: [" + decision.memoryType() + "] " + decision.content());
        }
        System.out.println();

        System.out.println("--- 问题 1：项目规范来自 RAG ---");
        System.out.println(">>> 用户: 任务系统使用什么数据库？");
        answer(c, "任务系统使用什么数据库？");

        System.out.println("--- 问题 2：用户长期偏好来自 Memory ---");
        System.out.println(">>> 用户: 我的项目用什么构建？");
        answer(c, "我的项目用什么构建？");

        System.out.println("--- 问题 3：同一条事实，两种身份（教材 8.4）---");
        System.out.println(">>> 用户: 项目使用什么构建工具？");
        answer(c, "项目使用什么构建工具？");

        db.close();
        System.out.println("==== 演示结束 ====");
    }

    private static void answer(Components c, String question) {
        QaResult result = c.agent().answer(question);
        System.out.println("Agent: " + result.answer());
        System.out.println();
    }

    private static void printImport(Components c) {
        System.out.println("[知识导入] 已导入 " + c.knowledge().count() + " 篇本地 Markdown 文档到 knowledge_doc 表：");
        for (var doc : c.knowledge().findAll()) {
            System.out.println("  - " + doc.title() + " (tags=" + doc.tags() + ")");
        }
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
            System.out.println("[警告] 未配置 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL，使用离线脚本模型（无法智能回答）。\n");
            llm = ScriptedLlmClient.of("（离线模式）未配置真实模型。请配置环境变量或根目录 .env 后重试。");
        }
        Components c = build(db, llm);
        printImport(c);

        Scanner scanner = new Scanner(System.in);
        System.out.println("输入问题直接提问；/memory 查看记忆；/knowledge 查看知识库；/exit 退出。");
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
            if (line.equals("/memory")) {
                var all = c.memories().findAll();
                if (all.isEmpty()) {
                    System.out.println("当前没有保存任何记忆");
                } else {
                    for (var m : all) {
                        System.out.println("- [" + m.type() + "] " + m.content());
                    }
                }
                continue;
            }
            if (line.equals("/knowledge")) {
                for (var d : c.knowledge().findAll()) {
                    System.out.println("- [" + d.title() + "] " + ContextBuilder.snippet(d.content()));
                }
                continue;
            }
            answer(c, line);
        }
        db.close();
        System.out.println("bye");
    }

    // ---------------- 装配 ----------------

    private record Components(
            Database db,
            MemoryRepository memories,
            KnowledgeRepository knowledge,
            MemoryExtractor extractor,
            RagQaAgent agent) {
    }

    private static Components build(Database db, LlmClient llm) {
        MemoryRepository memories = new MemoryRepository(db);
        KnowledgeRepository knowledge = new KnowledgeRepository(db);
        MemoryExtractor extractor = new MemoryExtractor(llm);
        KnowledgeImporter.importFromResources(knowledge);
        RagQaAgent agent = new RagQaAgent(
                llm,
                new MemoryRetriever(memories),
                new KnowledgeRetriever(knowledge),
                new ContextBuilder(),
                RagQaAgent.defaultSystemPrompt());
        return new Components(db, memories, knowledge, extractor, agent);
    }

    private static Database openDatabase() throws Exception {
        Files.createDirectories(Path.of("data"));
        return new Database(DB_URL);
    }
}
