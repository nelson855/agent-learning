# Stage 2 综合项目：Stateful Agent

## 前置

完成 Planning、Multi-turn、State、Memory。

## 项目目录

```text
stages/stage02-stateful-agent
```

在 Stage 1 思想上重新实现，不依赖 Stage 1 Module。

## 新增

```text
Conversation
Message Persistence
AgentRun
Plan
Replan
Long-term Memory
```

SQLite 持久化。

## 核心任务

用户第一次：

> 我的学习 Demo 都使用 Maven。帮我规划接下来三项 Agent 学习任务。

程序退出。

重新启动，新的 Conversation：

> 按我的习惯创建第一个学习项目，并告诉我为什么这样配置。

Agent 应能：

- 检索 Maven 偏好；
- 创建 Plan；
- 执行任务；
- 给出结果。

## Web UI：第一次引入可视化调试台

本 Stage 增加一个极简网页，但页面的目标不是“做聊天产品”，而是观察：

```text
Conversation ≠ State ≠ Memory ≠ Plan
```

### 页面至少包含

左侧聊天区：

- conversationId；
- 消息列表；
- 用户输入框；
- 新建 Conversation。

右侧观察区：

- Current Agent State；
- 当前 Plan 与 currentStep；
- 本轮检索到的 Memory；
- 最近一次 runId / status。

页面要让学习者一眼看出：

> 历史消息是一类数据，跨会话长期 Memory 是另一类数据。

## Web 技术

使用：

```text
JDK HttpServer
Jackson
HTML + CSS + 原生 JavaScript
```

不使用 Spring Boot、Vue、React、Node/npm。

建议入口：

```text
Main       → CLI（可保留）
WebMain    → 浏览器调试台
```

核心 Agent 服务由 CLI 和 Web 复用。

## 最小 API 语义

路径可调整，但至少能够表达：

```text
POST /api/chat
POST /api/conversations
GET  /api/conversations/{id}/state
GET  /api/conversations/{id}/memories
```

不要把 Agent Loop 写在 Handler 里。

## 重点验收

必须能够同时通过：

1. CLI / 后端测试证明状态与 Memory 逻辑正确；
2. 浏览器完成两轮会话；
3. 页面可观察 Conversation / State / Memory / Plan 的区别；
4. 关闭程序后 SQLite 仍保留长期 Memory；
5. 新 Conversation 能检索到之前写入的相关 Memory。

## AI 编程工具使用方式

开始实现前读取：

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/stages/Stage2_StatefulAgent.md
docs/prompts/stage02_stateful_agent.md
```

只修改本 Stage 模块，不回写历史 Lab / Stage。
