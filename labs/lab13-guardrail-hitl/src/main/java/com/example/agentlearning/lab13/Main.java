package com.example.agentlearning.lab13;

/**
 * lab13-guardrail-hitl 教学占位骨架。
 *
 * <p>本阶段只搭建 Maven 模块骨架，不实现任何 Agent 能力。
 * 后续由 {@code docs/prompts/13_guardrail_hitl.md} 实现本章内容。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }

    /** 供 smoke test 使用的最小确定性方法。 */
    public static String greeting() {
        return "hello from lab13-guardrail-hitl";
    }
}
