# 第 7 章：Memory——Agent 到底应该“记住”什么

## 7.1 最常见的混淆

下面三种东西不是一回事：

```text
最近 20 条消息
用户长期偏好
公司产品文档
```

它们分别更接近：

```text
Conversation Context
Long-term Memory
Knowledge / RAG
```

## 7.2 Short-term Memory

广义上可以指当前任务期间需要保留的运行信息：

- 最近对话
- 当前目标
- 当前 Plan
- Tool Result
- 临时事实

很多内容其实与 State 高度重合。

## 7.3 Long-term Memory

跨 Session 值得继续保留的信息。

例如：

```text
用户希望 Java Demo 使用 JDK 21
用户偏好 Maven
某项目数据库选择 SQLite
```

但千万不要：

> 每一句用户话都保存成永久 Memory。

Memory 需要选择。

## 7.4 Memory Write

一个简单流程：

```text
new message
↓
Memory Extractor
↓
是否值得长期保存？
├─ no
└─ yes
    ↓
 normalize
    ↓
 SQLite
```

学习 Demo 可以先用 LLM 提取：

```json
{
  "shouldRemember": true,
  "memoryType": "PREFERENCE",
  "content": "..."
}
```

程序再验证并保存。

## 7.5 Memory Retrieval

下一轮：

```text
current query
↓
搜索相关 Memory
↓
选 Top N
↓
加入 Context
```

第一版不用向量数据库。

SQLite 可以先实现：

- type filter
- keyword `LIKE`
- recency
- importance

这反而更容易理解 Retrieval 的本质。

## 7.6 Memory 表

```sql
CREATE TABLE memory (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    content TEXT NOT NULL,
    importance INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    last_used_at TEXT
);
```

## 7.7 本章 Demo

对话：

```text
用户：以后我的 Java Demo 都使用 Maven。
```

Memory Extractor 保存。

结束程序。

重新开始一个新 Conversation：

```text
用户：帮我初始化一个 Java Demo。
```

系统先检索 Memory，把：

```text
用户偏好：Java Demo 使用 Maven
```

放入 Context。

观察跨 Session 效果。

## 7.8 Memory 的真正难点

不是“存”。

而是：

```text
存什么？
什么时候存？
怎么去重？
什么时候更新？
什么时候忘？
什么时候取？
取多少？
```

这些问题远比数据库选型重要。

---

## 本章自测

1. 为什么 Conversation History 不等于 Long-term Memory？
2. 为什么不应该把所有消息永久保存成 Memory？
3. Memory Retrieval 的本质是什么？
4. 学习阶段为什么故意不用 Vector DB？

## 参考答案

1. History 是某次会话的过程记录；Long-term Memory 是经过选择后跨会话仍有价值的信息。
2. 会产生大量噪声、冲突、过时信息和上下文污染。
3. 根据当前任务，从所有记忆中选出真正相关的一小部分。
4. 先把“记忆选择和检索”机制看清楚，避免把 Memory 错误理解成“用了向量数据库”。
