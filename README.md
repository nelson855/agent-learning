# Agent Learning Repository — 教材与实验仓库（V3）

这是一套面向 Java 后端工程师的 AI Agent 工程学习仓库。

V3 在原有“单概念 Lab + 阶段综合 Stage”路线基础上，增加了 **阶段性 Web 可视化调试台**：Lab 继续优先使用 `Main` / CLI，Stage 2 开始用极简网页帮助观察 Agent 内部状态。

## 第一次使用

1. 保持当前目录结构不变。
2. 打开你习惯的 AI 编程工具。
3. 明确要求它先读取根目录 `AGENTS.md`。
4. 让它执行：

```text
docs/prompts/00_repository_bootstrap.md
```

5. 仓库骨架构建完成并通过 `mvn test` 后，从第一章开始：

```text
docs/chapters/01_LLM到Agent_无状态与上下文.md
docs/prompts/01_bootstrap_llm_basics.md
```

## 技术栈

```text
JDK 21
Maven
SQLite
JDBC
Jackson
JUnit 5
Java HttpClient
```

阶段综合项目需要 Web 可视化时，默认使用：

```text
JDK HttpServer（jdk.httpserver）
HTML + CSS + 原生 JavaScript
```

默认不使用 Spring Boot、Vue、React、Node/npm 和 Agent Framework，以便直接观察 Agent 核心机制。

## 文档入口

- `docs/00_教材总览.md`：整套教材是什么
- `docs/01_学习路线与Demo总览.md`：学习顺序和四个 Stage
- `docs/02_仓库架构与构建说明.md`：Lab / Stage / Maven Module 怎么组织
- `docs/03_技术基线与工程约定.md`：技术约束
- `docs/04_Web可视化调试台规范.md`：为什么只给 Stage 做前端，以及每个 Stage 展示什么
- `docs/chapters/`：概念教材
- `docs/prompts/`：可交给 AI 编程工具的实现任务
- `docs/stages/`：阶段综合项目设计说明

## Lab 与 Stage 的交互方式

```text
Lab 01 ~ Lab 14
→ Main / CLI 为主
→ 每次只暴露一个概念

Stage 1
→ CLI 综合验证

Stage 2
→ 极简聊天页 + State / Memory 可视化

Stage 3
→ Long-running Agent 调试台
→ Plan / Context / Compaction / Checkpoint / Resume / Evaluator

Stage 4
→ Mini Agent Harness 控制台
→ Tool / Memory / Trace / Approval / Worker 等综合观察
```

前端的目标是 **可观察 Agent**，不是训练前端开发能力。

## AI 编程工具

教材不绑定 Codex、Claude Code 或其他具体产品。只要工具能够读取/修改仓库并运行 Maven/Java 命令，就可以使用。

仓库维护同一份 AI 编程约束的双镜像：`AGENTS.md` 与 `CLAUDE.md`（内容一致，修改须同步）。
