# 第 3 章：Agent Loop 与 ReAct——Agent 真正开始“动起来”

## 3.1 最小 Agent

上一章：

```text
LLM
→ Tool Call
→ Tool Result
→ 结束
```

但很多问题需要连续动作。

例如：

```text
找到状态为 OPEN 的任务，并告诉我最早创建的那个任务标题。
```

可能需要：

```text
listOpenTasks
→ 得到任务列表
→ getTaskDetail
→ 最终回答
```

于是出现 Agent Loop：

```text
while (!finished) {
    response = llm(context);

    if (response.isFinal()) {
        return response;
    }

    toolResult = execute(response.toolCall());
    context.add(toolResult);
}
```

这就是很多 Agent 的骨架。

## 3.2 Observation

Tool Result 不只是“返回值”。

在 Agent 语义里，它是：

> Agent 对环境采取行动后看到的 Observation。

```text
Action: getTask("T-100")
Observation: status=OPEN, title=...
```

新的 Observation 会改变下一轮决策。

## 3.3 ReAct

ReAct 的核心不是要求打印一大段“思维链”，而是：

```text
Reason about current situation
→ Act
→ Observe
→ Continue
```

工程实现中不需要要求模型输出私有完整推理。

我们只需要结构化的：

```json
{
  "type": "tool_call",
  "tool": "getTask",
  "arguments": {...},
  "decisionSummary": "需要先获得任务详情"
}
```

或者：

```json
{
  "type": "final",
  "answer": "..."
}
```

## 3.4 Stop Condition

Agent Loop 最危险的错误之一：

```text
while (true)
```

必须至少具备：

```text
final answer
max steps
tool error policy
cancel
```

例如：

```java
int maxSteps = 8;
```

超过后：

```text
AGENT_MAX_STEPS_EXCEEDED
```

## 3.5 本章 Demo

让 Agent 完成：

> 创建一个“学习 Agent Loop”的任务，然后查询这个任务，最后把任务 ID 和状态告诉用户。

模型需要：

```text
createTask
→ getTask
→ final
```

运行时打印：

```text
STEP 1
MODEL ACTION: createTask
TOOL RESULT: ...

STEP 2
MODEL ACTION: getTask
TOOL RESULT: ...

STEP 3
FINAL: ...
```

这就是第一个真正值得称为 Agent 的 Demo。

## 3.6 这一章不要加什么

暂时不加：

- Plan
- Memory
- RAG
- Checkpoint
- Reflection
- Multi-Agent

因为你现在只需要看清：

```text
Context → Model Decision → Tool → Observation → Context
```

## 3.7 故障实验：制造死循环

写一个 FakeLlmClient，让它永远调用：

```text
getTask("NOT_FOUND")
```

确认 `maxSteps` 会停止。

你会理解：

> Agent 的自主性必须被 Harness 边界约束。

---

## 本章自测

1. Agent Loop 为什么是 Agent 的骨架？
2. Observation 是什么？
3. ReAct 和普通 Chain 有什么差异？
4. 为什么必须有 maxSteps？
5. 为什么 Demo 不要求打印模型完整思维链？

## 参考答案

1. 因为它让模型能够根据每次行动的新结果再次决策，从一次生成变成多步闭环。
2. Tool 或环境执行后返回给 Agent 的结果。
3. ReAct 在推理/决策与行动/观察之间反复交替，而固定 Chain 的路径通常由程序预先确定。
4. 防止错误决策或异常工具结果导致无限循环和无限成本。
5. Agent 工程需要可观察的决策与动作，不依赖暴露模型私有 chain-of-thought。
