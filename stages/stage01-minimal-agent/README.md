# stage01-minimal-agent

阶段整合一：手写一个 **控制台 AI 任务助手（Minimal Agent）**。

整合 Structured Output、Tool Calling、Agent Loop、ReAct、Workflow boundary 五块内容，
并把构成 Agent 的每个机制都写成独立、可见的类。

- 对应教材：`docs/chapters/02/03/04`，`docs/stages/Stage1_MinimalAgent.md`
- 对应 Prompt：`docs/prompts/stage01_minimal_agent.md`
- 包名：`com.example.agentlearning.stage01`

## 1. 本模块学什么

第一次完整看到并亲手搭建这样一条链：

```text
LLM → Tool Call → Tool Result → LLM → ... → Final Answer
```

本模块刻意只用 **CLI**，不加 HTTP / 页面，把 Agent Loop 这个核心观察点放到最大。

## 2. 必须自己实现的核心调用链

```text
LlmClient ──────────────────────────────────────────┐
   │  LLM 请求 / 回复                                │
   ▼                                                 │
AgentRunner ──► ToolExecutor ──► ToolRegistry ──► Tool（定义 + 执行器）
   │  (ReAct 循环)        (接口)    (登记/校验/分派)     │
   │  每步检查 StopCondition                        SQLite / 算术
   │                                                 │
   └─────────────────► Observation（[observation] 前缀）──┘
```

| 概念 | 类 | 职责 |
|---|---|---|
| LlmClient | `LlmClient` / `LlmResponse` / `Message` | 模型调用的唯一接缝，真实模型与 Fake 可切换 |
| ToolDefinition / ToolCall | `ToolDefinition` / `ToolCall` | 工具的"说明书"与一次调用请求 |
| ToolRegistry | `ToolRegistry` | 登记工具、参数校验、按名分派、生成工具说明 |
| ToolExecutor | `ToolExecutor` | Agent 循环依赖的接口（`ToolRegistry` 实现它） |
| AgentRunner | `AgentRunner` | ReAct 循环主体，打印结构化运行事件 |
| StopCondition | `StopCondition` / `MaxStepsStopCondition` | 循环何时必须停下（默认 maxSteps=8） |

## 3. 提供的工具

`TaskTools.createDefault(store)` 注册四个工具，任务存 SQLite（JDBC）：

| 工具 | 参数 | 作用 |
|---|---|---|
| `createTask` | `title`（必填）、`description`（可选） | 创建任务，初始状态 `OPEN`，返回 id |
| `getTask` | `id` | 按 id 查单个任务 |
| `listTasks` | 无 | 列出全部任务及状态 |
| `calculator` | `expression` | 四则运算（递归下降解析，无第三方库） |

## 4. 如何运行

```bash
mvn -pl stages/stage01-minimal-agent test        # 确定性测试（FakeLlmClient，不依赖真实模型）
mvn -pl stages/stage01-minimal-agent exec:java    # 交互式 Agent CLI
mvn -pl stages/stage01-minimal-agent exec:java -Dexec.args="--workflow"   # 固定 Workflow 对照
```

> `exec:java` 需根 `pom.xml` 配置 `exec-maven-plugin`；也可在 IDE 中直接运行 `Main.main()`。
> LLM 配置从环境变量或仓库根目录 `.env` 读取（`LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`）。
> 已配置 → 真实模型；未配置 → 自动退化为 `FakeLlmClient` 剧本演示。
> 数据落在 `./data/stage01.db`。

## 5. 运行时应该观察什么

### 交互式 Agent 模式

每一步输出结构化运行事件（不输出模型私有的推理过程）：

```text
>>> 用户: 创建两个 Agent 学习任务，然后告诉我现在一共有多少个 OPEN 任务。
STEP 1
MODEL_ACTION: TOOL_CALL
TOOL_CALL: createTask{title=Agent 学习任务一}
TOOL_RESULT: 已创建任务: Agent 学习任务一 (id=t-xxxx, status=OPEN)
STEP 2
...
FINAL: 目前一共有 2 个 OPEN 任务。
```

重点观察：**模型自己决定先调 createTask、再调 createTask、再调 listTasks 去数数量**——下一步做什么是模型基于上下文（含上一次工具结果）决定的，不是代码写死的。

### 对照实验（`--workflow`）

固定 Workflow 完成"相似"任务，打印：

```text
执行步骤       = generateTitle → createTask x2 → countOpen
model_call_count = 1          （模型只生成 1 次标题）
tool_call_count  = 0          （工具一个都不调）
open_count       = 2
```

对照结论：

```text
Workflow 的路径由程序决定：生成标题 → 创建任务 → 统计数量，顺序写死在代码里；
Agent 的下一步由模型基于状态决定：先调哪个工具、传什么参数、何时收尾，由模型看着上下文临场判断。
```

## 6. 本模块刻意没有实现什么

（Prompt / Stage 文档明确禁止，且不在本阶段教学目标内）

- **Web UI**：只用 CLI，防止 HTTP 和页面稀释 Agent Loop 这个观察点。
- **Memory / RAG**：每个输入是一个全新会话，上下文只有对话历史 + 工具观察。
- **Planner**：不做"先规划再执行"，就是一步一看的 ReAct。
- **Checkpoint / 断点续跑**：失败就失败，不恢复。
- **Multi-Agent / Agent Framework**：单 Agent，所有机制手写。

## 7. 测试

`AgentRunnerTest`（全部用 `FakeLlmClient` 剧本驱动，`jdbc:sqlite::memory:`）覆盖：

- 单 Tool 调用；
- 连续两个以上 Tool 调用；
- 未知 Tool（不崩溃，失败作为 Observation 交回）；
- 参数错误（缺参数被 `ArgumentValidator` 拦下）；
- 最大步数停止（剧本死循环，`MaxStepsStopCondition` 拦停）；
- 正常 Final Answer。

`WorkflowVsAgentTest` 验证对照：固定 Workflow 的路径与统计结果确定性成立。
