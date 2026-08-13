# 第 1 章：从 LLM 到 Agent——先理解“无状态”

## 1.1 一个容易产生的错觉

你和聊天模型连续聊十轮时，会感觉：

> “模型记得前面发生过什么。”

但在最基础的 API 模型里，更准确的理解是：

```text
response = LLM(current_context)
```

每一次调用都只根据“这一次传入的上下文”生成结果。

如果第二次调用只发送：

```text
User: 那它多少钱？
```

而没有把上一轮“它”指的是什么一起传入，模型并没有神秘的后台记忆可以依赖。

## 1.2 Chat API 做了什么

最常见的数据结构：

```text
[
  SystemMessage,
  UserMessage,
  AssistantMessage,
  UserMessage
]
```

应用程序负责保存这些 Message，并在下一次调用时重新传入。

所以：

```text
聊天连续性 ≠ 模型内部自动保存会话
聊天连续性 = 应用重新构造上下文
```

这是后面理解：

- Multi-turn
- Memory
- Context Compression
- Checkpoint

的基础。

## 1.3 普通 LLM 应用和 Agent 的边界

最普通应用：

```text
Input
  ↓
LLM
  ↓
Output
```

Agent 则开始出现：

```text
Goal
 ↓
LLM 决策
 ↓
Action
 ↓
Environment Result
 ↓
再次决策
```

区别不是“用了一个 Agent 类”，而是模型开始参与：

> **下一步行为选择。**

## 1.4 本章 Demo

实现一个极小控制台程序：

```text
用户输入
→ 追加到 messages
→ 调 LLM
→ 保存 assistant message
→ 下一轮继续
```

然后提供一个命令：

```text
/reset
```

直接清空 history。

你会立刻看到：

- 不清历史时模型能理解代词和上下文；
- 清历史后，这种“记忆”马上消失。

### 实现重点

先不要数据库。

只使用：

```java
List<Message>
```

因为本章要观察的就是：

> “多轮聊天其实是应用层重新提交 history。”

## 1.5 为什么现在不使用 SQLite

SQLite 后面用于真正持久化：

- Conversation
- State
- Memory
- Checkpoint

如果第一章就上数据库，你可能会把：

```text
history
```

和：

```text
long-term memory
```

混在一起。

## 1.6 观察实验

依次输入：

```text
我有一只猫，叫豆包。
```

然后：

```text
它叫什么？
```

随后执行：

```text
/reset
```

再问：

```text
它叫什么？
```

观察差异。

---

## 本章自测

1. LLM API 为什么可以粗略看成无状态函数？
2. 多轮对话的“连续性”主要由谁维护？
3. 一个程序调用了 LLM 就算 Agent 吗？
4. Agent 相比普通聊天应用新增的关键特征是什么？

## 参考答案

1. 因为一次生成主要由当前请求提供的上下文决定，基础调用不会自动读取你应用中的历史状态。
2. 应用程序/Harness，通过保存并重新发送消息历史。
3. 不算。普通问答也调用 LLM。
4. 模型开始参与下一步行为决策，并可能与工具或环境形成多步闭环。
