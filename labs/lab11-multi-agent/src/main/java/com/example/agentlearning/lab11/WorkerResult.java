package com.example.agentlearning.lab11;

/**
 * 一个 Worker 的执行结果（原样 JSON，未解析、未汇总）。
 *
 * @param name          Worker 名
 * @param contextChars  本 Worker 发送给模型的 context 字符数
 * @param raw           模型返回的原始 JSON 文本
 */
public record WorkerResult(String name, int contextChars, String raw) {

    public static WorkerResult empty(String name) {
        return new WorkerResult(name, 0, "");
    }
}