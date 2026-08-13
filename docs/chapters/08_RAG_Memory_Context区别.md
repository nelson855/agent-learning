# 第 8 章：RAG、Memory 与 Context——三个最容易混用的词

## 8.1 先用三个问题区分

### Context

> 这一次调用模型时，它实际能看到什么？

### Memory

> Agent 过去经历过什么，并且哪些内容值得以后继续使用？

### RAG / Knowledge

> 外部资料库里有什么与当前问题相关的知识？

## 8.2 Context 是“工作台”

一次模型请求可能有：

```text
System Prompt
最近消息
相关 Memory
RAG 文档
Tool Definitions
当前 Plan
Tool Results
```

这些最后全部汇聚成 Context。

所以：

> Context 是当前调用的输入集合，而不是某一种存储。

## 8.3 RAG

最简单 RAG：

```text
Question
↓
Retrieve documents
↓
Build Context
↓
LLM Answer
```

学习阶段可以用 SQLite 做全文简化检索。

例如表：

```text
knowledge_doc
- id
- title
- content
- tags
```

先使用：

```sql
LIKE '%keyword%'
```

不用急着引入 Embedding。

## 8.4 为什么 RAG 和 Memory 常被混淆

因为两者都可能：

```text
存文本
→ 检索
→ 放入 Prompt
```

但语义完全不同。

例如：

```text
“项目使用 JDK 21”
```

如果这是用户过去明确指定的偏好，它是 Memory。

如果这是项目技术规范文档中的内容，它是 Knowledge。

## 8.5 本章 Demo

准备三份本地 Markdown 文档：

```text
task-system-overview.md
database-rules.md
coding-rules.md
```

导入 SQLite。

用户：

```text
任务系统使用什么数据库？
```

RAG 查询知识库。

然后再问：

```text
我自己偏好什么构建工具？
```

Memory 查询。

把两种检索日志分别打印：

```text
MEMORY RETRIEVAL
RAG RETRIEVAL
```

## 8.6 核心判断

不要问：

> “这个内容存 Redis 还是向量数据库？”

先问：

> “这个内容从语义上到底属于什么？”

---

## 本章自测

1. Context 是存储系统吗？
2. RAG 和 Memory 的最大区别是什么？
3. 为什么两者都可能使用相同数据库，却仍不是同一个概念？
4. 一条信息既可能成为 Knowledge，也可能成为 Memory 吗？

## 参考答案

1. 不是。Context 是一次模型调用真正看到的信息集合。
2. RAG 面向外部知识，Memory 面向 Agent/用户过去经历与长期有价值信息。
3. 技术实现相似不代表语义职责相同。
4. 可以，取决于它的来源和用途；工程上仍应标记清楚来源与语义。
