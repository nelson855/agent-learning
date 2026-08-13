package com.example.agentlearning.lab02;

/**
 * 一个极简的四则运算求值器（递归下降解析），演示"工具执行完全由程序确定性完成"。
 *
 * <p>支持：数字（含小数）、括号、一元正负号，以及 {@code + - * /} 四则运算。
 * 不依赖任何第三方表达式库。表达式非法时抛出 {@link IllegalArgumentException}，
 * 由调用方（calculator 工具）转成失败结果。
 */
public final class Calculator {

    private final String src;
    private int pos;

    private Calculator(String src) {
        this.src = src;
    }

    /** 求值，例如 {@code "1+2*3"} → 7。 */
    public static double evaluate(String expression) {
        Calculator parser = new Calculator(expression);
        double result = parser.parseExpression();
        parser.skipSpaces();
        if (parser.pos < parser.src.length()) {
            throw new IllegalArgumentException("位置 " + parser.pos + " 有无法解析的内容");
        }
        return result;
    }

    /** expr := term (('+' | '-') term)* */
    private double parseExpression() {
        double value = parseTerm();
        while (true) {
            skipSpaces();
            if (peek() == '+') {
                pos++;
                value += parseTerm();
            } else if (peek() == '-') {
                pos++;
                value -= parseTerm();
            } else {
                return value;
            }
        }
    }

    /** term := factor (('*' | '/') factor)* */
    private double parseTerm() {
        double value = parseFactor();
        while (true) {
            skipSpaces();
            if (peek() == '*') {
                pos++;
                value *= parseFactor();
            } else if (peek() == '/') {
                pos++;
                value /= parseFactor();
            } else {
                return value;
            }
        }
    }

    /** factor := ('+'|'-') factor | '(' expr ')' | number */
    private double parseFactor() {
        skipSpaces();
        if (peek() == '+') {
            pos++;
            return parseFactor();
        }
        if (peek() == '-') {
            pos++;
            return -parseFactor();
        }
        if (peek() == '(') {
            pos++;
            double value = parseExpression();
            skipSpaces();
            if (peek() != ')') {
                throw new IllegalArgumentException("缺少右括号");
            }
            pos++;
            return value;
        }
        return parseNumber();
    }

    private double parseNumber() {
        skipSpaces();
        int start = pos;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            pos++;
        }
        if (start == pos) {
            throw new IllegalArgumentException("位置 " + pos + " 处不是数字");
        }
        return Double.parseDouble(src.substring(start, pos));
    }

    private void skipSpaces() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return pos < src.length() ? src.charAt(pos) : '\0';
    }
}
