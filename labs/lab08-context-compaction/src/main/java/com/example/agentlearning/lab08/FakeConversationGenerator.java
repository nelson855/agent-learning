package com.example.agentlearning.lab08;

/**
 * 假对话生成器：快速生成指定轮数的 user/assistant 对，制造足够长的历史触发压缩。
 * 内容固定（确定性），用于离线演示与观察，不走真实模型。
 */
public final class FakeConversationGenerator {

    private FakeConversationGenerator() {
    }

    public static void generate(CompactionService service, String conversationId, int rounds) {
        for (int i = 1; i <= rounds; i++) {
            service.appendUser(conversationId, "第 " + i + " 轮：请继续推进任务系统开发，把功能补齐。");
            service.appendAssistant(conversationId, "第 " + i + " 轮回复：已提交本轮的改动（round " + i + "）。");
        }
    }
}
