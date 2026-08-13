# 第 6 章：Multi-turn Conversation 与 Agent State

## 6.1 Message History 不等于完整 State

普通聊天可能只需要：

```text
messages
```

Agent 还需要：

```text
goal
plan
currentStep
toolResults
status
metadata
```

所以：

> Conversation History 是 State 的一部分，不是全部。

## 6.2 推荐数据模型

```text
Conversation
- conversationId
- createdAt

Message
- id
- conversationId
- role
- content
- createdAt

AgentRun
- runId
- conversationId
- goal
- status
- currentStep
- startedAt
- updatedAt
```

SQLite 非常适合做学习 Demo。

## 6.3 State Transition

把 Agent 看成状态机：

```text
State_n
  ↓
Model / Tool
  ↓
State_n+1
```

例如：

```text
RUNNING
→ WAITING_TOOL
→ RUNNING
→ COMPLETED
```

## 6.4 为什么显式 State 很重要

如果所有状态都只藏在 Prompt 文本里：

```text
目前做到第 3 步……
```

程序很难可靠：

- 恢复
- 查询进度
- 判断完成
- 统计
- 测试

所以：

> 重要运行状态应由程序保存，Prompt 只是把需要的信息呈现给模型。

## 6.5 本章 Demo

把第 3 章 Agent Loop 改造成：

```text
conversationId
+
runId
```

每一次：

```text
User Message
Assistant Message
Tool Call
Tool Result
Run Status
```

都保存到 SQLite。

提供 CLI：

```text
/chat new
/chat <conversationId>
/state <runId>
```

## 6.6 一个关键实验

程序运行两轮后直接退出 JVM。

重新启动。

从 SQLite 加载 Conversation History，再继续问：

```text
我们刚才创建的任务是什么？
```

这时你会第一次看到：

> “持久化多轮会话”与“模型本身记住”是两回事。

---

## 本章自测

1. Message History 和 Agent State 有什么区别？
2. 为什么不能把所有 Agent 状态只存在 Prompt 里？
3. Agent State 为什么适合用状态机思维理解？
4. 程序重启后继续对话依靠的是什么？

## 参考答案

1. History 主要描述对话消息；State 还包含目标、计划、当前步骤、运行状态、工具结果等。
2. 因为程序无法稳定查询、修改、恢复、测试这些状态。
3. 每次模型或工具执行都会把一个明确状态变成另一个状态。
4. 应用从持久化存储恢复历史和状态，再重新构造给模型的上下文。
