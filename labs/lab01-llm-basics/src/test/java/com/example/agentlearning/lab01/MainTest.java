package com.example.agentlearning.lab01;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * lab01 验收测试：全部使用 {@link FakeLlmClient}，不依赖真实网络。
 */
class MainTest {

    /** 连续两轮时，第二次请求必须携带第一轮的历史消息。 */
    @Test
    void secondRequestIncludesHistory() {
        FakeLlmClient fake = new FakeLlmClient();
        Conversation conversation = new Conversation(fake);

        conversation.sendUserMessage("我有一只猫，叫豆包。");
        conversation.sendUserMessage("它叫什么？");

        List<Message> secondRequest = fake.lastRequest();
        // user(猫) + assistant(上一轮回复) + user(它叫什么)
        assertEquals(3, secondRequest.size());
        assertEquals(Role.USER, secondRequest.get(0).role());
        assertEquals("我有一只猫，叫豆包。", secondRequest.get(0).content());
        assertEquals("它叫什么？", secondRequest.get(2).content());
    }

    /** /reset 清空 history 后，新一轮请求只包含当前这一条用户消息。 */
    @Test
    void resetClearsHistory() {
        FakeLlmClient fake = new FakeLlmClient();
        Conversation conversation = new Conversation(fake);

        conversation.sendUserMessage("第一轮");
        assertFalse(conversation.history().isEmpty());

        conversation.reset();
        assertTrue(conversation.history().isEmpty());

        conversation.sendUserMessage("reset 后的第一轮");
        assertEquals(1, fake.lastRequest().size());
        assertEquals("reset 后的第一轮", fake.lastRequest().get(0).content());
    }

    /** 缺失配置时应快速失败，而不是用空 Key 发出网络请求。 */
    @Test
    void constructorFailsFastWhenConfigMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAiCompatibleLlmClient(null, "key", "model"));
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAiCompatibleLlmClient("http://localhost:8000/v1", null, "model"));
    }
}
