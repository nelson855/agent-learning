# Stage 1 综合项目：Minimal Agent

## 前置章节

完成第 1~4 章。

## 项目目录

```text
stages/stage01-minimal-agent
```

## 目标

整合：

```text
Structured Output
Tool Calling
Agent Loop
ReAct
Workflow boundary
```

做一个 **控制台 AI 任务助手**。

## 为什么这一阶段故意不做前端

Stage 1 最重要的是第一次完整看到：

```text
LLM → Tool Call → Tool Result → LLM → ... → Final
```

如果此时加入 HTTP 和页面，会稀释 Agent Loop 这个核心观察点。因此 Stage 1 只做 CLI。

## Tools

至少：

```text
createTask
getTask
listTasks
calculator
```

SQLite 保存任务。

## 必须自己实现

```text
LlmClient
ToolRegistry
ToolExecutor
AgentRunner
StopCondition
```

## 禁止

```text
Memory
RAG
Planner
Checkpoint
Multi-Agent
Web UI
Agent Framework
```

## 最终验收任务

用户：

> 创建两个 Agent 学习任务，然后告诉我现在一共有多少个 OPEN 任务。

Agent 应可以多步调用工具完成。

同时提供一个固定 Workflow 完成相似任务，比较程序控制和模型控制。

CLI 中要清楚打印：

```text
STEP
MODEL ACTION
TOOL NAME
TOOL ARGS
TOOL RESULT
FINAL ANSWER
```

不要输出隐藏 chain-of-thought。

## AI 编程工具使用方式

开始实现前读取：

```text
AGENTS.md
docs/stages/Stage1_MinimalAgent.md
docs/prompts/stage01_minimal_agent.md
```

只修改本 Stage 模块，不回写历史 Lab。
