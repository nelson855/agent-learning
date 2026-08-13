package com.example.agentlearning.lab01;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvFileTest {

    @Test
    void parseReadsKeyValueLines() {
        String content = """
                # LLM 配置
                LLM_BASE_URL=https://api.example.com/v1
                LLM_API_KEY = sk-abc
                LLM_MODEL="gpt-4o"

                """;
        Map<String, String> values = EnvFile.parse(content);
        assertEquals("https://api.example.com/v1", values.get("LLM_BASE_URL"));
        assertEquals("sk-abc", values.get("LLM_API_KEY"));
        assertEquals("gpt-4o", values.get("LLM_MODEL"));
    }

    @Test
    void parseIgnoresCommentsAndBlankLines() {
        Map<String, String> values = EnvFile.parse("# 只有注释\n\n  \n");
        assertTrue(values.isEmpty());
    }

    @Test
    void getReturnsNullWhenNeitherEnvNorFileHasKey() {
        // 用随机键名规避本机环境变量或 .env 中已有同名键的情况
        String key = "LLM_UNKNOWN_" + Math.abs(System.nanoTime());
        assertNull(EnvFile.get(key));
    }
}
