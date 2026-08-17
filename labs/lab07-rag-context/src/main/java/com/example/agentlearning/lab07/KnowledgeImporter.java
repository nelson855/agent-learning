package com.example.agentlearning.lab07;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 把本地 Markdown 文档导入 {@code knowledge_doc} 表。
 *
 * <p>三份文档固定从 classpath 的 {@code /knowledge/*.md} 读取，格式约定：
 * <pre>
 * # 标题
 * Tags: sqlite, database
 *
 * 正文...
 * </pre>
 * 导入是幂等的：先清空知识表再重新插入（教学简化，无增量导入）。
 */
public final class KnowledgeImporter {

    public static final List<String> FILES = List.of(
            "task-system-overview.md",
            "database-rules.md",
            "coding-rules.md");

    private KnowledgeImporter() {
    }

    /** 导入全部文档，返回成功导入的数量。 */
    public static int importFromResources(KnowledgeRepository repository) {
        repository.deleteAll();
        int imported = 0;
        for (String file : FILES) {
            String markdown = readResource("/knowledge/" + file);
            KnowledgeDoc doc = parse(file, markdown);
            repository.insert(doc);
            imported++;
        }
        return imported;
    }

    /** 解析一份 Markdown：标题、标签、正文。 */
    public static KnowledgeDoc parse(String fileName, String markdown) {
        String title = fileName;
        String tags = "";
        List<String> contentLines = new ArrayList<>();

        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        int i = 0;
        if (lines.length > 0 && lines[0].startsWith("# ")) {
            title = lines[0].substring(2).trim();
            i = 1;
        }
        if (i < lines.length && lines[i].trim().startsWith("Tags:")) {
            tags = lines[i].trim().substring("Tags:".length()).trim();
            i++;
        }
        for (; i < lines.length; i++) {
            contentLines.add(lines[i]);
        }

        String content = String.join("\n", contentLines).strip();
        return new KnowledgeDoc(
                "k-" + fileName.replace(".md", ""),
                title,
                content,
                tags,
                Instant.now().toString());
    }

    private static String readResource(String path) {
        try (InputStream in = KnowledgeImporter.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("找不到知识文档: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取知识文档失败: " + path, e);
        }
    }
}
