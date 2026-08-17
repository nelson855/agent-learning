package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 确定性脚本模型：按顺序吐出预设回复，剧本耗尽后重复最后一条。
 * 用于离线 Demo 和测试，不依赖真实在线模型。
 */
public final class ScriptedLlmClient implements LlmClient {

    private final List<String> script;
    private int index;

    public ScriptedLlmClient(List<String> script) {
        if (script == null || script.isEmpty()) {
            throw new IllegalArgumentException("script 不能为空");
        }
        this.script = List.copyOf(script);
    }

    public static ScriptedLlmClient of(String... lines) {
        return new ScriptedLlmClient(List.of(lines));
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        String reply = script.get(Math.min(index, script.size() - 1));
        index++;
        return new LlmResponse(reply);
    }

    public int callCount() {
        return index;
    }
}
