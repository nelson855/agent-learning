# 综合实现 Prompt：Stage 02 Stateful Agent + Web Debugger

> 与具体 AI 编程工具无关。只实现 `stages/stage02-stateful-agent`。

## 开始前读取

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/chapters/05_Planning与PlanReplan.md
docs/chapters/06_多轮对话与AgentState.md
docs/chapters/07_Memory_短期长期与检索.md
docs/stages/Stage2_StatefulAgent.md
```

## 核心目标

在一个独立 Stage 中整合：

```text
Agent Loop
Conversation
Message Persistence
Agent State
Plan / Replan
Long-term Memory
```

数据持久化到本 Stage 自己的 SQLite 文件。

## 第一步：先完成无 Web 的核心层

先让下面的核心能力可以通过 JUnit + `FakeLlmClient` 独立验证：

```text
ConversationService
AgentApplicationService
AgentRunner
Plan state
MemoryStore / MemoryRetriever
Repositories
```

核心层不得依赖 `HttpServer`。

## 第二步：增加极简 Web Adapter

使用 JDK `HttpServer`，提供 `WebMain`。

页面使用 HTML/CSS/原生 JavaScript，至少显示：

```text
左：Conversation / Chat
右：Current State / Plan / Retrieved Memory
```

至少支持：

- 新建 Conversation；
- 发送消息；
- 查看当前 State；
- 查看 Plan；
- 查看本轮 Retrieved Memory；
- 程序重启后开启新 Conversation，再验证长期 Memory 被取回。

## API

保持最少数量。可以参考：

```text
POST /api/conversations
POST /api/chat
GET  /api/conversations/{id}/state
GET  /api/conversations/{id}/memories
```

不要为了 REST 风格拆大量 Handler / DTO。

## 关键教学要求

README 必须专门解释并给数据库例子：

```text
Conversation History 是什么
Agent State 是什么
Long-term Memory 是什么
Plan 是什么
它们为什么不能混成一个 messages 列表
```

## 禁止

```text
Spring Boot
Vue / React / Node
RAG
Context Compaction
Checkpoint
Multi-Agent
```

## 验收

1. `mvn test`；
2. `FakeLlmClient` 场景通过；
3. `WebMain` 能启动；
4. 浏览器能完成核心任务；
5. 页面数据来自后端真实 SQLite / State；
6. CLI/Web 不复制两套 Agent 核心逻辑。
