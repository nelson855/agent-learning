package com.example.agentlearning.lab07;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检索用分词器（极简，非真实中文分词）：
 * <ul>
 *   <li>连续英文/数字 → 一个词块（如 {@code Java}、{@code Maven}）；</li>
 *   <li>连续汉字 → 按二元组切分（如"任务系统使用什么数据库" → 任务/务系/系统/…/据库），
 *       让 LIKE 子串匹配能命中长中文问句里的关键词。</li>
 * </ul>
 * 教学简化：不做 Embedding 语义检索，分词只是为了让 keyword/LIKE 尽量可用。
 */
public final class Tokens {

    private static final Pattern WORD = Pattern.compile("[a-zA-Z0-9]+");
    private static final Pattern CHINESE = Pattern.compile("[\\u4e00-\\u9fff]+");

    private Tokens() {
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return tokens;
        }
        Matcher word = WORD.matcher(text);
        while (word.find()) {
            tokens.add(word.group());
        }
        Matcher chinese = CHINESE.matcher(text);
        while (chinese.find()) {
            String block = chinese.group();
            if (block.length() == 1) {
                tokens.add(block);
            } else {
                for (int i = 0; i + 2 <= block.length(); i++) {
                    tokens.add(block.substring(i, i + 2));
                }
            }
        }
        return tokens;
    }
}
