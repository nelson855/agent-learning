package com.example.agentlearning.lab11;

import java.util.List;

/**
 * 一个 Worker：拥有独立 System Prompt 与独立 Context，只负责自己那部分数据。
 *
 * <p>{@code run} 把「自己的 Prompt + 自己的上下文片段」发给模型，
 * 拿到结构化 JSON 原样返回，不做汇总——汇总由 {@link Orchestrator} 负责。
 */
public interface Worker {

    String name();

    String systemPrompt();

    /** 只包含本 Worker 关注的数据片段（Context 隔离的教学点）。 */
    String buildContext(TaskStats stats);

    default WorkerResult run(LlmClient llm, TaskStats stats) {
        String sys = systemPrompt();
        String ctx = buildContext(stats);
        String raw = llm.chat(List.of(Message.system(sys), Message.user(ctx))).content();
        return new WorkerResult(name(), sys.length() + ctx.length(), raw);
    }
}