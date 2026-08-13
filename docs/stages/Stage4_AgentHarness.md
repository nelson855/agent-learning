# Stage 4 综合项目：Mini Agent Harness

## 前置

完成全部章节。

## 项目目录

```text
stages/stage04-agent-harness
```

## 目标

不要追求生产级。

实现一个能让你从架构上看懂现代 Agent 系统的小型 Harness，并提供一个专门观察 Harness 的 Web Console。

## 核心组件

```text
ModelClient
AgentRunner
ContextBuilder
ToolProvider
ToolExecutor
StateStore
MemoryStore
KnowledgeStore
Planner
CheckpointStore
ApprovalService
TraceService
Evaluator
```

可选：

```text
WorkerAgent
Orchestrator
SkillLoader
McpAdapter（教学级）
```

## 统一原则

每个组件必须在 README 中回答：

> 如果删除这个组件，系统会失去什么能力？

## 最终 Demo

用户提交一个复杂任务：

> 读取本地知识文档，规划任务，创建多个子任务，分析已有失败任务，必要时调用不同 Worker，生成总结。删除或高风险操作必须等待人工批准。程序中断后可以恢复。

## Web UI：Mini Agent Harness Console

这个页面不是普通聊天页，而是“小型 Agent 运行控制台”。

### 至少包含

```text
Task / Conversation

Run & Plan
- run status
- current step
- plan

Tool Events
- tool name
- args
- result / error
- elapsed time

Memory / Knowledge
- retrieved memory
- retrieved docs

Approval Queue
- requested action
- risk / reason
- approve
- reject

Trace Timeline
- MODEL_CALL
- TOOL_CALL
- TOOL_RESULT
- STATE_CHANGED
- MEMORY_RETRIEVED
- CHECKPOINT_SAVED
- APPROVAL_REQUIRED
- EVALUATION

Metrics
- step count
- tool call count
- elapsed time
```

如果实现 Multi-Agent，再增加：

```text
Worker / Handoff
- worker name
- assigned task
- status
- handoff summary
```

## 不展示什么

页面不得展示或要求模型返回隐藏 chain-of-thought。

只展示可公开的运行事件、结构化决策摘要和程序状态。

## Human-in-the-loop

高风险 Tool 必须真正停在 `WAITING_APPROVAL` 一类状态，直到网页执行 Approve / Reject。

不能只在页面上做一个假的确认弹窗，后端却已经执行了动作。

## Web 技术

仍然保持：

```text
JDK HttpServer
HTML + CSS + 原生 JavaScript
```

不升级为 Spring Boot / React。

原因：最终目标仍然是理解 Harness，而不是展示 Web 技术。

## 最终学习成果

完成后，再选择一个 Java Agent Framework 重做同一需求。

对比：

```text
哪些代码被框架替代？
哪些概念仍然存在？
哪些行为更难观察？
哪些生产能力变简单？
```

这一步才是正式进入框架学习的起点。

## AI 编程工具使用方式

开始实现前读取：

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/stages/Stage4_AgentHarness.md
docs/prompts/stage04_agent_harness.md
```

只修改本 Stage 模块，不回写历史 Lab / Stage。
