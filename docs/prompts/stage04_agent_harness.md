# 综合实现 Prompt：Stage 04 Mini Agent Harness + Web Console

> 与具体 AI 编程工具无关。只实现 `stages/stage04-agent-harness`。

## 开始前读取

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/chapters/12_MultiAgent_Orchestrator与Handoff.md
docs/chapters/13_Tool_Skill_MCP.md
docs/chapters/14_Guardrail_HITL与Sandbox.md
docs/chapters/15_Observability_Tracing与Evaluation.md
docs/stages/Stage4_AgentHarness.md
```

## 目标

把已经学习过的机制组织成一个教学级 Mini Agent Harness。

不是生产框架，不追求插件生态。

## Part A：明确 Harness 组件边界

至少实现：

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

README 为每个组件回答：

> 删除它后，Agent 会失去什么能力？

## Part B：Trace Event

定义简单、明确的 TraceEvent 类型，例如：

```text
MODEL_CALL
TOOL_CALL
TOOL_RESULT
STATE_CHANGED
MEMORY_RETRIEVED
CONTEXT_BUILT
CHECKPOINT_SAVED
APPROVAL_REQUIRED
APPROVAL_RESOLVED
EVALUATION
RUN_FINISHED
```

不要记录 hidden chain-of-thought。

Trace 持久化 SQLite。

## Part C：Human-in-the-loop

提供至少一个高风险 Tool，例如“删除教学任务”。

执行链必须真正变成：

```text
Tool requested
→ WAITING_APPROVAL
→ user approves/rejects
→ only then execute/cancel
```

不能先执行再弹确认。

## Part D：可选 Multi-Agent

如果实现 Worker / Orchestrator，保持 2~3 个 Worker 即可，并让 Handoff 可追踪。

不要为了展示 Multi-Agent 搭复杂消息总线。

## Part E：Web Harness Console

使用 JDK HttpServer + 原生 HTML/JS。

页面至少展示：

```text
Task / Conversation
Run / Plan
Tool Events
Memory / Knowledge
Approval Queue
Trace Timeline
Metrics
```

若实现 Worker，再显示 Worker/Handoff。

Approval Queue 必须能真正调用后端 Approve / Reject。

## 关键教学要求

页面上的每一个主要 Panel 都在 README 标注它对应的 Harness 组件：

```text
Trace Timeline → TraceService
Approval Queue → ApprovalService
Memory Panel → MemoryStore
Tool Events → ToolExecutor
...
```

这样学习者可以从 UI 反向定位后端架构。

## 禁止

```text
Spring Boot
Vue / React / Node
微服务拆分
Kafka
Redis
生产级权限系统
复杂插件框架
```

## 验收

至少完成一个复杂任务，能够观察：

- plan；
- 多次 tool call；
- knowledge / memory retrieval；
- 一次 approval；
- checkpoint；
- trace timeline；
- evaluator；
- 最终结果。

自动测试至少覆盖 approval 前不得执行高风险 Tool，以及关键 TraceEvent 顺序。
