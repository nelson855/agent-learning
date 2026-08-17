package com.example.agentlearning.lab07;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * LLM 配置读取：环境变量优先，其次仓库根目录的 {@code .env} 文件。
 */
public final class EnvFile {

    public static final String BASE_URL = "LLM_BASE_URL";
    public static final String API_KEY = "LLM_API_KEY";
    public static final String MODEL = "LLM_MODEL";

    private EnvFile() {
    }

    public static Map<String, String> load() {
        Map<String, String> env = new HashMap<>(loadDotEnv(findRepoRoot()));
        for (String key : new String[] {BASE_URL, API_KEY, MODEL}) {
            String value = System.getenv(key);
            if (value != null && !value.isBlank()) {
                env.put(key, value);
            }
        }
        return env;
    }

    private static Path findRepoRoot() {
        Path dir = Path.of("").toAbsolutePath().normalize();
        while (dir != null) {
            if (Files.exists(dir.resolve("pom.xml")) && Files.exists(dir.resolve("AGENTS.md"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return Path.of(".");
    }

    private static Map<String, String> loadDotEnv(Path root) {
        Map<String, String> result = new HashMap<>();
        Path dotEnv = root.resolve(".env");
        if (!Files.exists(dotEnv)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(dotEnv)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                result.put(trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 .env 失败: " + dotEnv, e);
        }
        return result;
    }
}
