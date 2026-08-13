package com.example.agentlearning.lab03;

/**
 * lab03-agent-loop-react 教学占位骨架。
 *
 * <p>本阶段只搭建 Maven 模块骨架，不实现任何 Agent 能力。
 * 后续由 {@code docs/prompts/03_agent_loop_react.md} 实现本章内容。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }

    /** 供 smoke test 使用的最小确定性方法。 */
    public static String greeting() {
        return "hello from lab03-agent-loop-react";
    }
}
