# 实现 Prompt 03：Agent Loop + ReAct

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

阅读：

- `AGENTS.md`
- `docs/chapters/03_AgentLoop与ReAct.md`

本次只实现/修改模块：

```text
labs/lab03-agent-loop-react
```

## 目标

手写一个 Agent Loop。

核心接口可自行设计，但必须清楚暴露：

```text
Context
Model Decision
Tool Call
Tool Result / Observation
Loop
Stop Condition
```

## Demo 任务

让 Agent 能完成：

> 创建一个“学习 Agent Loop”的任务，然后再次查询这个任务，最终返回任务 ID 与状态。

必须可能产生：

```text
createTask
→ getTask
→ final
```

## 约束

- 最大步数默认 8
- 每一步打印：
  - step
  - action type
  - tool name
  - tool result summary
- 不要求模型输出完整思维链
- 允许 `decisionSummary` 这种简短解释
- 增加 `FakeLlmClient` 死循环用例，验证 maxSteps

不要加入 Plan、Memory、RAG、Checkpoint。
