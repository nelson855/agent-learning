package com.example.agentlearning.lab02;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从 {@code .env} 文件读取配置（KEY=VALUE），作为环境变量的本地兜底。
 *
 * <p>取值优先级：<b>环境变量 &gt; 仓库附近的 {@code .env} 文件</b>。
 * {@code .env} 通过从当前工作目录逐级向上查找，方便在 IDE / 命令行 / 任意模块目录下运行。
 */
public final class EnvFile {

    private static volatile Map<String, String> fileCache;

    private EnvFile() {
    }

    /** 取一个配置项：先看环境变量，再看 .env 文件；都没有返回 null。 */
    public static String get(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return fileValues().get(key);
    }

    /**
     * 解析 .env 文本：支持空行、{@code #} 注释、{@code KEY=VALUE}，值两侧可带引号。
     * 纯函数，便于单元测试。
     */
    public static Map<String, String> parse(String content) {
        Map<String, String> values = new HashMap<>();
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            values.put(key, unquote(value));
        }
        return values;
    }

    private static Map<String, String> fileValues() {
        Map<String, String> cached = fileCache;
        if (cached == null) {
            cached = loadFromFile();
            fileCache = cached;
        }
        return cached;
    }

    private static Map<String, String> loadFromFile() {
        Path dotEnv = findDotEnv();
        if (dotEnv == null) {
            return Map.of();
        }
        try {
            List<String> lines = Files.readAllLines(dotEnv, StandardCharsets.UTF_8);
            return parse(String.join("\n", lines));
        } catch (IOException e) {
            throw new IllegalStateException("读取 .env 失败: " + dotEnv, e);
        }
    }

    /** 从当前工作目录向上查找 .env，找不到返回 null。 */
    private static Path findDotEnv() {
        Path dir = Paths.get("").toAbsolutePath().normalize();
        while (dir != null) {
            Path candidate = dir.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
