# 实现 Prompt 07：RAG 与 Context Builder

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/08_RAG_Memory_Context区别.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab07-rag-context
```

## 目标

使用 SQLite 实现极简 Knowledge Store。

准备：

```text
knowledge_doc
```

允许从本地 Markdown 文本导入。

第一版检索只用：

```text
keyword / LIKE
tags
```

不要引入 Embedding 和 Vector DB。

## ContextBuilder

显式实现：

```text
System Prompt
+ Recent Messages
+ Retrieved Memory
+ Retrieved Knowledge
+ Current Request
```

运行时打印：

```text
MEMORY RETRIEVAL
RAG RETRIEVAL
CONTEXT SUMMARY
```

## 教学要求

用同一个问题分别证明：

- 用户长期偏好来自 Memory
- 项目规范来自 RAG

README 必须解释为什么它们底层都可以存 SQLite，但语义不同。
