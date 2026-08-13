# 学习路线与 Demo 总览

## 一、这套路线不是“框架教程”

我们不从：

```text
Spring AI
LangChain4j
LangGraph
```

开始。

而是从最小机制开始：

```text
LLM
→ Structured Output
→ Tool
→ Loop
→ State
→ Memory
→ Context
→ Planning
→ Evaluation
→ Harness
→ Multi-Agent
```

最后再看框架。

## 二、四次阶段整合

### Stage 1：Minimal Agent

在学习：

- Structured Output
- Tool Calling
- Agent Loop
- ReAct

之后完成。

目标：

> 手写一个最小的、真的可以自主选择工具并连续执行的 Agent。

交互：CLI。此阶段故意不做网页。

### Stage 2：Stateful Agent

在学习：

- Workflow
- Planning
- Multi-turn
- State
- Memory

之后完成。

目标：

> 从“一次性 Agent”升级为“能够持续处理会话和任务”的 Agent。

交互：增加极简 Web 聊天页，同时显示 Conversation、Plan、State、Memory。

### Stage 3：Long-running Agent

在学习：

- RAG
- Context Engineering
- Compression
- Checkpoint
- Evaluator

之后完成。

目标：

> 让 Agent 能够执行更长任务，并且不会因为上下文膨胀、程序崩溃、输出错误而轻易失败。

交互：升级为 Long-running Agent 调试台，可观察 Context、Compaction、Checkpoint、Resume、Evaluator。

### Stage 4：Agent Platform

学习：

- Multi-Agent
- Skill
- MCP
- Guardrail
- Observability

之后完成。

目标：

> 形成一个小型 Agent Harness，并理解现代 Agent 平台的大体结构。

交互：升级为 Mini Agent Harness Console，可观察 Tool、Trace、Approval、Memory，以及可选 Worker/Handoff。

## 三、推荐节奏

每一章建议花费的“学习单位”不是按时间，而按完成状态判断。

只有当你能够不看教材回答：

> “这个概念解决什么问题？如果不用会怎样？代码中它在哪里？”

才进入下一章。

## 四、Demo 索引

| 章节 | Demo | 重点 |
|---|---|---|
| 01 | LLM Baseline | LLM 是无状态计算 |
| 02 | Tool Calling | 模型输出驱动程序行为 |
| 03 | ReAct Agent | Tool Loop |
| 04 | Workflow | 程序控制 vs 模型控制 |
| 05 | Planner | Plan / Execute / Replan |
| 06 | Conversation State | 多轮对话与 State |
| 07 | Memory | SQLite 持久化记忆 |
| 08 | RAG | 外部知识检索 |
| 09 | Context Compaction | 上下文选择与压缩 |
| 10 | Checkpoint | 中断恢复 |
| 11 | Evaluator | 结果验收与优化 |
| 12 | Multi-Agent | Orchestrator / Worker |
| 13 | Skill / MCP | 能力封装与协议 |
| 14 | Guardrail | 权限与人类确认 |
| 15 | Tracing / Eval | 可观测性与评估 |


## 五、代码仓库推进方式

学习代码统一放在一个 Git 仓库，但每个实验和阶段项目使用独立 Maven Module：

```text
labs/labXX-*       # 单概念实验
stages/stageXX-*   # 阶段综合项目
```

Lab 不从前一个 Lab 继承代码历史；Stage 也不是在旧 Stage 上持续打补丁，而是参考已学机制重新整合。

推荐执行顺序：

```text
先执行 docs/prompts/00_repository_bootstrap.md
→ 阅读章节
→ 执行对应实现 Prompt
→ 运行该模块 Main
→ 完成观察题/自测
→ 到阶段节点执行 `docs/prompts/stageXX_*.md` 综合项目 Prompt
```

## 六、CLI 与 Web 的边界

```text
Lab 01~14        Main / CLI
Stage 1          CLI
Stage 2          Web Chat Debugger
Stage 3          Long-running Debugger
Stage 4          Harness Console
```

Web 规范见 `docs/04_Web可视化调试台规范.md`。不要给单概念 Lab 批量添加前端。
