# AGENTS.md — AI Agent Learning Repository

## 1. 你正在维护什么

这是一个 **AI Agent 教学实验仓库**，不是生产系统。

目标是帮助 Java 后端开发者逐层理解 Agent 的底层工程机制。

无论当前由哪一种 AI 编程工具执行任务，都必须遵守本文件。

优先级从高到低：

1. 教学清晰度
2. 概念可观察性
3. 可运行
4. 测试可验证
5. 代码整洁
6. 工程完备性

如果“工程最佳实践”和“教学可理解性”冲突，优先教学可理解性。

## 2. 本仓库唯一的 AI 编程规则文件

本仓库只维护：

```text
AGENTS.md
```

不要主动创建或复制：

```text
CLAUDE.md
CODEX.md
AI_INSTRUCTIONS.md
.github/copilot-instructions.md
```

或其他针对某个 AI 编程产品的重复规则文件，除非用户以后明确要求。

实现每个 Prompt 时，工具应显式读取本文件。

## 3. 固定技术栈

默认必须使用：

- JDK 21
- Maven
- SQLite（本地文件数据库）
- JDBC
- Jackson
- JUnit 5
- Java 标准 `HttpClient` 优先

当 Stage 明确要求 Web 可视化时，默认使用：

- JDK 自带 `com.sun.net.httpserver.HttpServer`（`jdk.httpserver`）
- HTML
- CSS
- 原生 JavaScript
- 静态资源放 `src/main/resources/web/`

除非具体 Prompt 明确要求，否则禁止主动引入：

- Spring Boot
- Spring MVC / WebFlux
- Spring AI
- LangChain4j
- LangGraph
- ORM / Hibernate / MyBatis
- Redis
- Kafka / MQ
- Docker
- Kubernetes
- Vue / React / Angular
- Node.js / npm / pnpm / yarn
- 大型 DI 框架
- 复杂插件系统

Web 页面是教学可视化层，不是新的技术学习主线。

## 4. 仓库架构

整个项目采用：

```text
一个 Git 仓库
+ 一个根 Maven 聚合工程
+ 多个独立 Lab Maven Module
+ 多个独立 Stage Maven Module
```

标准目录：

```text
agent-learning/
├── AGENTS.md
├── README.md
├── pom.xml
├── docs/
├── labs/
└── stages/
```

根 `pom.xml` 只做聚合与版本管理，不承载 Agent 业务代码。

每个 Lab / Stage 都应该能够作为一个独立 Maven Module 被理解和运行。

## 5. Lab 与 Stage 的代码组织原则

### 5.1 Lab

每个 Lab 是一个单概念教学实验。

不要为了复用而让多个 Lab 形成复杂继承或依赖关系。

允许复制少量基础代码，例如：

- `Message`
- `LlmClient`
- `OpenAiCompatibleLlmClient`
- JSON 工具类

“少量重复”优于“为了消除重复而隐藏概念”。

前半程禁止主动创建：

```text
agent-common
agent-core
agent-runtime
agent-spi
```

等公共 Maven Module。

Lab 默认通过 `Main.main()` / CLI 运行。**不要给每个 Lab 创建网页。**

### 5.2 Stage

Stage 是阶段综合项目。

它可以参考已完成 Lab 的思想重新实现，但：

- Stage 不依赖 Lab Module；
- 新 Stage 不应直接依赖旧 Stage；
- 不要通过不断给同一个工程打补丁来形成 Stage 2/3/4；
- 每个 Stage 都应能独立看出本阶段新增的机制。

交互层按阶段递进：

```text
Stage 1：CLI
Stage 2：简单聊天页 + State / Memory 可视化
Stage 3：Long-running Agent 调试台
Stage 4：Mini Agent Harness 综合控制台
```

Stage 的 Web 层必须薄，核心 Agent 逻辑不得写进 HTTP Handler 或 JavaScript。

## 6. Web 可视化的教学边界

### 6.1 Web UI 的目的

Web UI 只用于让下面这些内部机制“看得见”：

- Conversation / Message
- Plan / Current Step
- Tool Call / Tool Result
- State Change
- Memory Retrieval
- RAG Retrieval
- Context Selection
- Compaction
- Checkpoint / Resume
- Evaluator Feedback
- Trace
- Approval

页面不是为了产品化，不追求组件库、动画、复杂设计系统或响应式框架。

### 6.2 分层必须保持

正确结构：

```text
Browser
  ↓ HTTP/JSON
Thin Web Adapter
  ↓
Application / Agent Service
  ↓
AgentRunner / Tool / State / Memory / ...
```

禁止：

```text
HttpHandler 里实现 Agent Loop
JavaScript 里决定 Tool
页面状态替代 SQLite Agent State
```

CLI 和 Web 应尽量调用同一套核心应用服务。

### 6.3 前端实现约束

默认：

```text
src/main/resources/web/index.html
src/main/resources/web/app.js
src/main/resources/web/styles.css
```

如果一个 `index.html` 就能讲清楚，也允许把 CSS/JS 内联以减少文件数量。

默认不需要：

- SPA Router
- 状态管理库
- bundler
- TypeScript
- npm
- WebSocket

需要动态刷新时优先简单轮询；只有当前 Stage 的教学目标确实需要实时事件流时才考虑 SSE。

## 7. 核心教学边界

### 7.1 不要擅自增加当前章节没有要求的 Agent 能力

例如当前章节只学习 Tool Calling，不要自动加入：

- Memory
- RAG
- Planner
- Vector DB
- Multi-Agent
- Reflection Loop

### 7.2 不要把模型决策偷偷改成硬编码

如果本章要观察“模型如何选择 Tool”，不能写：

```java
if (userInput.contains("任务")) {
    callTaskTool();
}
```

除非这是为了与 Agent 方案做对照实验。

### 7.3 能用确定性代码验证的内容，要保留确定性验证

例如：

- JSON 解析 / Schema
- 参数类型
- SQL 执行结果
- 文件是否存在
- 测试是否通过

不要用第二次 LLM 调用替代所有程序校验。

### 7.4 不输出隐藏推理链

Demo / Web UI 可以展示：

- 选择了什么 Tool
- Tool 参数
- Tool Result
- Plan
- State Change
- Evaluator Feedback
- 简短 decision summary

不要要求模型输出私有 chain-of-thought。

需要解释行为时，使用简短结构化字段，例如：

```text
decision_summary
reason_code
next_action
```

## 8. 数据库约定

SQLite 默认使用 module 内本地文件：

```text
./data/<module-name>.db
```

必须：

- 使用 `CREATE TABLE IF NOT EXISTS`
- 启动时自动初始化 schema
- SQL 保持简单可读
- Repository 层不要过度抽象
- 测试允许使用临时 SQLite 文件或 `:memory:`
- SQLite 数据文件不提交 Git

Web 页展示的 State / Memory / Trace 应来自后端真实状态，不要在浏览器内伪造一套第二状态源。

## 9. LLM 调用约定

代码通过环境变量读取：

```text
LLM_BASE_URL
LLM_API_KEY
LLM_MODEL
```

不要把 Key 写入仓库。

优先保留一个供应商无关的：

```java
LlmClient
```

接口，让 Demo 可以：

- 接真实模型观察智能行为；
- 接 `FakeLlmClient` 做确定性测试。

测试不得依赖真实在线模型才能通过。

## 10. 每个 Lab / Stage 必须提供

```text
README.md
pom.xml
src/main/java/...
src/test/java/...
```

README 至少说明：

1. 本模块学什么；
2. 为什么需要这个概念；
3. 如何运行；
4. 运行时应该观察什么；
5. 本模块刻意没有实现什么。

Lab 优先提供简单的 `Main.main()` 控制台入口。

Stage 2~4 如果 Prompt 要求 Web UI，还必须说明：

- Web 启动入口；
- 本地访问地址；
- 页面每个区域对应哪个 Agent 概念；
- CLI / Web 是否复用同一核心服务。

## 11. 实现 Prompt 的统一执行规则

收到某一章或某一 Stage 的实现 Prompt 后：

1. 先读取本文件；
2. 再读取 Prompt 指定的教材章节；
3. 如果是 Stage，再读取对应 `docs/stages/` 文件；
4. 如果 Stage 有 Web UI，再读取 `docs/04_Web可视化调试台规范.md`；
5. 只实现当前 Prompt 范围；
6. 先给出简短文件修改计划；
7. 实现代码；
8. 运行测试；
9. 运行至少一个可观察 Demo；
10. 最后汇报：
   - 创建/修改了什么；
   - 测试结果；
   - Demo 观察结果；
   - 页面如何观察本 Stage 的 Agent 机制；
   - 哪些能力故意没有实现。

如果 Prompt 与本文件冲突，以用户当前明确要求为最高优先级，其次是当前 Prompt，再其次是本文件。

## 12. 什么时候允许重构

只有满足以下任一条件时：

- 当前 Prompt 明确要求；
- 重复代码已经妨碍理解；
- 综合项目阶段确实需要合并概念。

重构时必须保留“概念边界”，不能因为抽象后让学习者看不到：

```text
Agent Loop
State Transition
Tool Dispatch
Context Build
Memory Retrieval
Checkpoint Save
Trace Event
```

这些核心过程。
