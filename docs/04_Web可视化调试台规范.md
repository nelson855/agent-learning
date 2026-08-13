# Web 可视化调试台规范

## 1. 为什么 V3 增加 Web 页面

前面的 Lab 仍然优先使用 CLI，因为单概念实验最重要的是：

> 用最少代码看清一个机制。

但当 State、Memory、Plan、Checkpoint、Trace 等机制组合后，只看最终控制台回答会丢失大量信息。

因此 Stage 2~4 增加一个极简 Web UI。它的角色不是“前端产品”，而是：

> **Agent 的可视化调试器。**

## 2. 总体原则

```text
Lab      → CLI，看单个机制
Stage 1  → CLI，看最小 Agent Loop
Stage 2  → Chat UI，看会话 / State / Memory / Plan
Stage 3  → Debug UI，看长任务 / Context / Checkpoint / Resume / Evaluator
Stage 4  → Harness UI，看 Tool / Agent / Trace / Approval 等系统级机制
```

不要为了页面漂亮引入额外技术栈。

## 3. 默认 Web 技术

后端：

```text
JDK 21
com.sun.net.httpserver.HttpServer
Jackson
```

前端：

```text
HTML
CSS
Vanilla JavaScript
fetch()
```

静态资源建议：

```text
src/main/resources/web/
├── index.html
├── app.js
└── styles.css
```

如果页面很小，也允许全部写进一个 `index.html`。

默认禁止：

```text
Spring Boot
Vue / React / Angular
Node / npm
TypeScript
Vite / Webpack
UI 组件库
```

## 4. Web 层应该多薄

理想结构：

```text
WebServer / HttpHandler
    ↓ 只做 HTTP / JSON 转换
AgentApplicationService
    ↓
AgentRunner
    ↓
Tool / State / Memory / Context / Checkpoint / Trace
```

Web 层不实现 Agent 决策。

如果把 Web 层删掉，核心 Agent 应仍然可以通过 CLI 或测试运行。

## 5. Stage 2 页面：Stateful Agent Chat

### 页面目标

让学习者直观看到：

> “聊天记录”和“真正的 Agent State / Memory”不是同一回事。

### 推荐布局

左侧：

```text
Conversation
- User message
- Assistant message
- 输入框
```

右侧：

```text
Current State
- conversationId
- runId
- status
- currentPlanStep

Plan
- [x] step 1
- [>] step 2
- [ ] step 3

Retrieved Memory
- key / value / source
```

### 最小 API

可自行调整路径，但语义保持简单：

```text
POST /api/chat
GET  /api/state?conversationId=...
GET  /api/memories?conversationId=...
```

不要为了“REST 规范”增加复杂 Controller 分层。

## 6. Stage 3 页面：Long-running Agent Debugger

### 页面目标

让学习者看到一个长任务为什么需要：

```text
Context Selection
Compaction
Checkpoint
Resume
Validation
Evaluator
```

### 推荐区域

```text
任务输入

Run Overview
- runId
- status
- current step
- start / resume

Plan

Context Inspector
- retrieved docs
- memories
- selected context
- compacted summary

Checkpoint Timeline
- version
- savedAt
- currentStep
- Resume 按钮

Validation / Evaluator
- validation result
- feedback
```

### 教学操作

页面必须允许：

1. 启动一个多步骤任务；
2. 人为模拟一次中断；
3. 查看最近 Checkpoint；
4. 重新启动 / Resume；
5. 观察 Agent 从哪个步骤继续。

“模拟 crash”可以安全地做成教学按钮或可控异常，不要真正杀掉 IDE 或系统进程。

## 7. Stage 4 页面：Mini Agent Harness Console

### 页面目标

把 Harness 从抽象词变成可以观察的运行时系统。

### 推荐区域

```text
Task / Conversation

Run & Plan

Tool Events
- tool name
- args
- result / error
- duration

Memory / Knowledge

Workers / Handoff（如果实现）

Approval Queue
- action
- risk
- approve / reject

Trace Timeline
- MODEL_CALL
- TOOL_CALL
- TOOL_RESULT
- STATE_CHANGED
- CHECKPOINT_SAVED
- APPROVAL_REQUIRED
- EVALUATION

Metrics
- step count
- tool count
- elapsed time
```

页面不展示模型隐藏 chain-of-thought。

## 8. 页面设计优先级

从高到低：

1. 看得出 Agent 做了什么；
2. 看得出状态在哪里变化；
3. 能手动制造教学场景；
4. 信息分区清楚；
5. 页面美观。

“漂亮但看不到内部过程”视为失败。

## 9. 测试要求

Web 功能至少应具备：

- HTTP Handler / Application Service 的基础测试；
- 核心 Agent 测试不依赖 Web Server；
- `FakeLlmClient` 可以驱动确定性场景；
- UI 不作为唯一验收方式。

自动测试的重点仍然是后端确定性逻辑。

## 10. 启动体验

Stage 2~4 推荐保持一个简单入口，例如：

```java
public final class WebMain {
    public static void main(String[] args) {
        // init sqlite
        // init agent core
        // start HttpServer
        // print http://localhost:8080
    }
}
```

如果端口冲突，可通过环境变量或参数覆盖。

README 必须写清：

```text
1. 怎么启动
2. 浏览器打开什么地址
3. 测试哪几个场景
4. 页面每个区域对应什么 Agent 概念
```
