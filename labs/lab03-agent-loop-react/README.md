# lab03-agent-loop-react — Agent Loop 与 ReAct

- 对应教材：`docs/chapters/03_AgentLoop与ReAct.md`
- 对应 Prompt：`docs/prompts/03_agent_loop_react.md`

## 这个 Lab 证明什么

1. **Agent Loop 是 Agent 的骨架**：上一章是"一次 LLM → 一次工具 → 结束"，这一章升级为
   `Context → Model Decision → Tool Call → Observation → Context` 的**闭环**——模型看到每次行动的观察结果后，能继续决策，直到完成任务；
2. **Observation 不是普通返回值**：工具结果是 Agent"对环境采取行动后看到的观察"，必须追加进上下文、在下一轮重新提交给模型，下一轮决策才可能正确；
3. **自主性必须被 Harness 边界约束**：模型可能陷入死循环，`maxSteps`（默认 8）是最后一道防线——这是本章最重要的工程教训。

## 本章自己实现的核心概念

| 概念 | 类 | 职责 |
| --- | --- | --- |
| 上下文 | `AgentContext` | 消息历史，追加用户输入 / 模型决策 / 工具观察 |
| 模型决策 | `AgentDecision` | `tool_call`（工具+参数+简短 `decisionSummary`）或 `final`（最终回答） |
| 决策解析 | `AgentDecisionParser` | 把模型 JSON 回复解析成决策 |
| 工具/观察 | `ToolRegistry` + `ToolResult` | 执行工具，结果作为 Observation 回到上下文 |
| 循环 | `AgentLoop` | `while (!finished)` 多步执行，每步打印 step / action / tool / result |
| 停止条件 | `AgentLoop` | `final` 回答、`maxSteps`（超限返回 `AGENT_MAX_STEPS_EXCEEDED`）、工具错误不崩溃不静默 |
| 步骤轨迹 | `StepTrace` / `AgentRun` | 记录每一步，供观察与测试断言 |

## 决策协议（写进 system prompt）

```json
{"type":"tool_call","tool":"getTask","arguments":{"taskId":"t-xxx"},"decisionSummary":"需要先获得任务详情"}
{"type":"final","answer":"任务 id=t-xxx，状态=pending"}
```

教学边界：**不要求模型输出完整思维链**，只允许 `decisionSummary` 这类一句话解释。

## 如何运行

### 离线观察（默认）

未配置环境变量时自动使用 `ScriptedLlmClient`，不访问网络：

```bash
cd labs/lab03-agent-loop-react
mvn test
```

### 接真实模型观察智能行为

在仓库根目录 `.env` 填入 `LLM_API_KEY` 后，运行 `Main.main()`（或命令行），输入 Demo 目标：

```
创建一个“学习 Agent Loop”的任务，然后再次查询它，最后告诉我任务 ID 与状态
```

观察输出中的 `STEP 1 → STEP 2 → STEP 3(FINAL)`，即 `createTask → getTask → final`。

## 运行时应该观察什么

1. 每步打印的 `STEP` / `ACTION TYPE` / `TOOL NAME` / `TOOL RESULT`；
2. 真实模型会从 createTask 的观察里读出任务 id，再用它调用 getTask —— 这就是 Observation 驱动下一轮决策；
3. 看测试 `infiniteLoopStopsAtMaxSteps`：一个永远调用 `getTask("NOT_FOUND")` 的模型，被 `maxSteps=8` 拦停，返回 `AGENT_MAX_STEPS_EXCEEDED`。

## 刻意没有实现

- **Plan / 预规划**：模型每步即兴决策，没有长期计划（下一章 lab05）；
- **Memory / RAG / Checkpoint / Reflection / Multi-Agent**：都留到后续章节；
- **工具结果不再次喂回模型？不，正好相反**——Observation 每次都回到上下文，这是本章的意义所在；只是没有持久化。

本 Lab 只观察一件事：**Agent 第一次真正"动起来"**，且它的自主性被 `maxSteps` 约束。
