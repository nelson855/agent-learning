# 第 9 章：Context Engineering 与 Compaction

## 9.1 Prompt Engineering 不够了

短任务里你关注：

> Prompt 怎么写？

长任务里更重要的问题变成：

> 这一次到底应该把哪些信息给模型？

这就是 Context Engineering 的核心。

## 9.2 Context Budget

假设你拥有：

```text
200 条 Message
80 条 Tool Result
30 条 Memory
20 篇知识文档
1 个 Plan
```

显然不能永远全部塞进去。

即便模型 Context Window 足够，也会出现：

- 成本增加
- 延迟增加
- 关键信息被噪声淹没
- 模型关注错重点

所以：

> Context Window 大，不代表应该填满。

## 9.3 Context Builder

建议显式写一个类：

```java
ContextBuilder
```

输入：

```text
current request
conversation
state
memory
knowledge
plan
recent tool results
```

输出：

```text
List<Message>
```

这会让你第一次看到：

> Prompt 其实是 Agent Harness 的“编译产物”。

## 9.4 Selection

先选择：

```text
最近 N 条消息
最相关的 Memory
最相关文档
尚未完成 Plan
最近 Tool Result
```

不要一次全给。

## 9.5 Compaction

当历史超过阈值：

```text
old messages
↓
summarizer
↓
conversation_summary
```

新的 Context：

```text
System
Summary
Recent Messages
Relevant Memory
Current State
```

## 9.6 Summary 必须包含什么

学习 Demo 中强制结构：

```json
{
  "goal": "...",
  "completed": [],
  "importantFacts": [],
  "decisions": [],
  "openQuestions": [],
  "pendingActions": []
}
```

这样比一段随意摘要更容易恢复。

## 9.7 Externalization

更进一步：

不要把所有工作进展都塞 Prompt。

把这些东西外部化：

```text
plan 表
task 表
checkpoint 表
artifact 文件
progress.md
```

需要时读取。

这也是长任务 Agent 的关键思路：

> Context 是缓存，不是数据库。

## 9.8 本章 Demo

人为制造 30~50 轮对话。

程序设置：

```text
MAX_RECENT_MESSAGES = 10
COMPACT_AFTER = 20
```

超过时：

```text
老消息 → Summary → SQLite
```

之后构造：

```text
Summary + 最近 10 条
```

CLI 打印：

```text
RAW HISTORY COUNT
CONTEXT MESSAGE COUNT
SUMMARY VERSION
```

让压缩结果可观察。

---

## 本章自测

1. Context Engineering 比 Prompt Engineering 多关注了什么？
2. 为什么 Context Window 很大也不应该全塞？
3. Compaction 和简单删除历史有什么区别？
4. 为什么说 Context 是缓存，不是数据库？

## 参考答案

1. 关注当前调用应该选择哪些历史、记忆、知识、状态和工具信息。
2. 会增加成本、延迟和噪声，并可能降低模型对关键内容的关注。
3. Compaction 尝试保留目标、决定、事实和未完成事项，而删除会直接丢失信息。
4. 长期真实状态应存外部系统，Context 只是在某一次推理时加载需要的信息。
