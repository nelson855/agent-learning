# 综合实现 Prompt：Stage 01 Minimal Agent

> 与具体 AI 编程工具无关。只实现 `stages/stage01-minimal-agent`。

## 开始前读取

```text
AGENTS.md
docs/chapters/02_StructuredOutput与ToolCalling.md
docs/chapters/03_AgentLoop与ReAct.md
docs/chapters/04_WorkflowPatterns.md
docs/stages/Stage1_MinimalAgent.md
```

## 任务

把 Stage 01 占位模块实现为一个可以运行的 CLI Minimal Agent。

必须自己实现并让调用关系清楚可见：

```text
LlmClient
ToolDefinition / ToolCall
ToolRegistry
ToolExecutor
AgentRunner
StopCondition
```

至少提供：

```text
createTask
getTask
listTasks
calculator
```

任务数据使用 SQLite JDBC。

## CLI 可观察性

每一步输出结构化运行事件：

```text
STEP
MODEL_ACTION
TOOL_CALL
TOOL_RESULT
FINAL
```

不要输出 chain-of-thought。

## 对照实验

额外实现一个固定 Workflow 完成一个相似任务，用 README 解释：

```text
Workflow 的路径由程序决定
Agent 的下一步由模型基于状态决定
```

## 本 Stage 禁止

```text
Web UI
Memory
RAG
Planner
Checkpoint
Multi-Agent
Agent Framework
```

## 测试

必须使用 `FakeLlmClient` 覆盖：

- 单 Tool；
- 连续两个以上 Tool；
- 未知 Tool；
- 参数错误；
- 最大步数停止；
- 正常 Final Answer。

最后运行本 module 测试和至少一个 CLI Demo，并汇报执行轨迹。
