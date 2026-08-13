package com.example.agentlearning.lab04;

import java.time.Instant;
import java.util.UUID;

/**
 * 任务业务规则：两版共用的<b>确定性</b>逻辑。
 *
 * <p>这是本章的核心论点——"把确定性留给程序"：无论走 Workflow 还是 Agent，
 * 标题/描述怎么算合法、任务 id 怎么生成、怎么落库，都由程序统一决定，不交给模型自由发挥。
 */
public final class TaskRules {

    public static final int TITLE_MAX = 40;
    public static final int DESCRIPTION_MAX = 200;

    private TaskRules() {
    }

    /** 校验标题/描述是否合法；合法返回 null，否则返回错误消息。 */
    public static String validate(String title, String description) {
        if (title == null || title.isBlank()) {
            return "标题不能为空";
        }
        if (title.length() > TITLE_MAX) {
            return "标题过长（最多 " + TITLE_MAX + " 字，实际 " + title.length() + "）";
        }
        if (description == null || description.isBlank()) {
            return "描述不能为空";
        }
        if (description.length() > DESCRIPTION_MAX) {
            return "描述过长（最多 " + DESCRIPTION_MAX + " 字，实际 " + description.length() + "）";
        }
        return null;
    }

    /** 生成任务 id、初始状态为 pending，写入存储并返回任务行。 */
    public static Task save(TaskStore store, String title, String description) {
        String id = "t-" + UUID.randomUUID().toString().substring(0, 8);
        Task task = new Task(id, title, description, "pending", Instant.now().toString());
        store.insert(task);
        return task;
    }
}
