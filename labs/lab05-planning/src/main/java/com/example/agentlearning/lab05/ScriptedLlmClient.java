package com.example.agentlearning.lab05;

import java.util.List;

/**
 * 剧本式 LLM 客户端：按预设回复依次返回，用于确定性测试与离线演示。
 *
 * <p>剧本耗尽后<b>重复最后一条</b>，方便制造反复失败的场景来观察
 * Replan 上限拦停。
 */
public final class ScriptedLlmClient implements LlmClient {

    private final List<String> script;
    private int index;

    public ScriptedLlmClient(String... script) {
        if (script.length == 0) {
            throw new IllegalArgumentException("剧本不能为空");
        }
        this.script = List.of(script);
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        String reply = script.get(Math.min(index, script.size() - 1));
        index++;
        return new LlmResponse(reply);
    }
}
