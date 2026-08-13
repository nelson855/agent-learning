# 第 10 章：Agent Harness、Checkpoint 与 Long-running Agent

## 10.1 Harness 是什么

如果把 LLM 看成大脑，Harness 更像：

> 让这个大脑可以安全、连续、可恢复地工作的运行底座。

一个成熟 Harness 可能包含：

```text
Model Client
Context Builder
Agent Loop
Tool Registry
Tool Executor
State Store
Memory
Checkpoint
Retry
Guardrail
Tracing
Compaction
Budget
Stop Condition
```

## 10.2 为什么 Harness 是工程概念

ReAct、Planning 更像“Agent 怎么思考/行动”的模式。

Harness 解决：

> “这个 Agent 到底怎么被系统运行起来？”

## 10.3 Checkpoint

假设执行：

```text
S1 DONE
S2 DONE
S3 RUNNING
```

此时 JVM 崩溃。

没有 Checkpoint：

```text
从头开始
```

有 Checkpoint：

```text
load latest checkpoint
→ 恢复 State
→ 从 S3 继续
```

## 10.4 Checkpoint 保存什么

至少：

```text
runId
goal
plan
currentStep
state
important tool results
context summary pointer
updatedAt
```

不一定要保存所有 Context 文本。

## 10.5 SQLite 表

```text
agent_checkpoint
- id
- run_id
- version
- state_json
- created_at
```

每次重要状态变化后：

```text
save new version
```

而不是覆盖唯一一行。

这样可以观察版本历史。

## 10.6 Resume

启动：

```text
/resume <runId>
```

流程：

```text
load latest checkpoint
↓
validate
↓
rebuild runtime state
↓
continue agent loop
```

## 10.7 本章 Demo

设计 5 步任务。

执行到第 3 步时支持命令：

```text
/crash
```

直接模拟异常退出。

再次启动：

```text
/resume run-xxx
```

应该从最近 Checkpoint 继续。

## 10.8 Long-running Agent 的关键

长任务不是：

> 把 maxSteps 从 8 改成 800。

而是必须组合：

```text
State
Checkpoint
Context Compaction
Externalized Progress
Retry
Budget
Evaluation
```

---

## 本章自测

1. Harness 和 ReAct 为什么不是同一层概念？
2. Checkpoint 和 Conversation History 有什么区别？
3. 为什么 Checkpoint 最好保留 version？
4. 为什么长任务不能只靠更大的 Context Window？

## 参考答案

1. ReAct 是决策/行动模式，Harness 是运行、状态、工具、权限、恢复等外围系统。
2. History 记录聊天过程，Checkpoint 保存“能恢复执行”的关键运行状态。
3. 可以查看历史、回滚、调试状态演进，也避免覆盖后丢失事故现场。
4. 长任务还存在状态恢复、失败重试、外部进度、成本、噪声等问题。
