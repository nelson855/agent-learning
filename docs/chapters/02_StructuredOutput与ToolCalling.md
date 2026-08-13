# 第 2 章：Structured Output 与 Tool Calling——让文字变成程序动作

## 2.1 从“回答”到“控制”

普通 LLM 输出：

```text
我建议你查询任务 123。
```

程序很难可靠执行。

结构化输出：

```json
{
  "action": "QUERY_TASK",
  "taskId": "123"
}
```

程序就可以：

```java
switch (action) {
    case "QUERY_TASK" -> taskService.query(taskId);
}
```

这一步非常关键：

> LLM 不再只是生成给人看的文本，而是在生成给程序消费的决策。

## 2.2 Structured Output 解决什么

主要解决：

- 输出字段稳定
- 程序可解析
- 参数可验证
- 行为可分派

但注意：

> “要求返回 JSON”不等于“JSON 一定正确”。

所以程序必须做：

```text
parse
validate
reject / retry
```

## 2.3 Tool Calling

Tool 可以理解为 Agent 能调用的外部能力。

例如：

```text
getTask(taskId)
createTask(title)
calculate(expression)
```

模型拿到的不是 Java 方法本身，而是类似：

```json
{
  "name": "getTask",
  "description": "根据 taskId 查询任务",
  "parameters": {
    "taskId": "string"
  }
}
```

模型决定：

```text
我要调用 getTask
```

Harness 再把它映射成真正 Java 方法。

## 2.4 Tool Definition 为什么重要

一个 Tool 的质量至少取决于：

```text
名称
描述
参数 Schema
返回值
错误语义
权限
```

如果定义：

```text
doSomething()
```

模型很难选。

如果定义：

```text
getTaskById(taskId)
```

并清楚说明什么时候使用，模型选择会稳定得多。

## 2.5 本章 Demo

创建三个 Tool：

```text
getTask
createTask
calculator
```

其中任务数据使用本地 SQLite。

表：

```sql
CREATE TABLE IF NOT EXISTS task (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

但这一章只做：

```text
一次 LLM 决策
→ 最多一次 Tool 调用
→ 输出 Tool Result
```

**不要循环。**

因为循环属于下一章。

## 2.6 你应该刻意制造的错误

### 错误一：参数缺失

让 Fake LLM 返回：

```json
{
  "name": "getTask",
  "arguments": {}
}
```

程序应该拒绝执行，而不是 NPE。

### 错误二：不存在的 Tool

```text
deleteAllTasks
```

ToolRegistry 应明确返回：

```text
UNKNOWN_TOOL
```

### 错误三：错误类型

```json
{
  "taskId": 123
}
```

如果 schema 要求 string，应由程序验证。

## 2.7 一条重要原则

> 模型负责“建议执行什么”，程序负责“这个动作能不能执行、如何执行”。

Tool Calling 不等于把系统权限交给模型。

---

## 本章自测

1. Structured Output 为什么是 Agent 开发的重要基础？
2. Tool Calling 时，模型是否真的直接执行了 Java 方法？
3. Tool 参数为什么必须由程序验证？
4. Tool Description 写得模糊会有什么后果？
5. 这一章为什么故意不做 Agent Loop？

## 参考答案

1. 它把自然语言决策转换成程序可以可靠解析的结构。
2. 不是。模型只产生 Tool Call 意图，Harness/应用程序负责真实执行。
3. 模型输出不是可信输入，可能缺字段、类型错误甚至构造不存在的参数。
4. 模型更容易误选、不选或传错参数。
5. 为了把“单次模型决策”和“多步循环执行”两个概念分开观察。
