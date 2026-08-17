# 编码规范
Tags: coding, rules, maven, build

本项目使用 Maven 作为构建工具（不是 Gradle）。
开发环境固定为 JDK 21。
禁止引入 Spring 系列、LangChain4j、Node.js 等重型框架。
Web 可视化层必须薄，核心 Agent 逻辑不得写在 HTTP Handler 或 JavaScript 里。
