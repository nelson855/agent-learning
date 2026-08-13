# 实现 Prompt 06：Conversation State + SQLite Memory

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

这是两个小阶段，建议按顺序实现。

阅读：

- `docs/chapters/06_多轮对话与AgentState.md`
- `docs/chapters/07_Memory_短期长期与检索.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab06-state-memory
```

## Phase A：State

SQLite 建议表：

```text
conversation
message
agent_run
```

实现：

```text
/chat new
/chat <conversationId>
/state <runId>
```

程序重启后可以恢复 Conversation。

## Phase B：Memory

新增：

```text
memory
```

实现最简单的：

```text
MemoryExtractor
MemoryRepository
MemoryRetriever
```

先用：

- type
- keyword
- recency
- importance

不要使用向量数据库。

## Demo

Session A：

```text
以后我的 Java Demo 都使用 Maven。
```

保存 Memory。

程序退出。

Session B：

```text
帮我初始化一个 Java Demo。
```

检索并展示：

```text
RETRIEVED MEMORY
```

## 验收

测试必须区分：

```text
Conversation History
Long-term Memory
Agent State
```

三者不能只是同一张表换名字。
