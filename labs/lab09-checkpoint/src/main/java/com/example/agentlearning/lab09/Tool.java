package com.example.agentlearning.lab09;

/**
 * 一个可被 Agent 调用的工具。
 *
 * <p>本模块为了<b>确定性可复现</b>，工具全部是纯函数式实现（不依赖 LLM 决策），
 * 这样 /crash 与 /resume 的测试可以精确断言"哪一步被跳过、哪一步被重做"。
 */
@FunctionalInterface
public interface Tool {

    /** 执行工具，返回对 Agent 可见的结果文本。 */
    String execute(String args);
}
