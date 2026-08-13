# 综合实现 Prompt：Stage 03 Long-running Agent + Debugger

> 与具体 AI 编程工具无关。只实现 `stages/stage03-long-running-agent`。

## 开始前读取

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/chapters/08_RAG_Memory_Context区别.md
docs/chapters/09_ContextEngineering与Compaction.md
docs/chapters/10_Harness_Checkpoint与长任务.md
docs/chapters/11_Reflection_Evaluator与确定性校验.md
docs/stages/Stage3_LongRunningAgent.md
```

## 目标

实现一个可以：

```text
检索知识
选择上下文
必要时压缩上下文
逐步执行计划
保存 Checkpoint
模拟中断
Resume
确定性校验
Evaluator 反馈并有限重试
```

的 Long-running Agent。

## 实现顺序

### Part A：核心长任务引擎

先不用 Web，实现并测试：

```text
KnowledgeStore / Retriever
MemoryRetriever
ContextBuilder
Compactor
CheckpointStore
RunService
Validator
Evaluator
```

### Part B：确定性中断与 Resume

设计一个“教学中断”机制，例如指定执行到第 N 步后把 run 标记为 interrupted 并抛出受控异常。

要求：

- Checkpoint 已经持久化；
- 新进程/新对象可以从 SQLite load；
- Resume 不重新执行已经完成的步骤。

### Part C：Web Debugger

使用 `WebMain + HttpServer`。

页面至少包含：

```text
Run Overview
Plan
Context Inspector
Checkpoint Timeline
Validation / Evaluator
```

并提供：

```text
Start
Simulate Interruption
Resume
```

按钮或等价操作。

## Context Inspector 必须区分

```text
RAG retrieved documents
Memory retrieved items
Selected context
Compacted summary
```

不要只显示一个大字符串。

## Checkpoint 可观察性

每个 checkpoint 至少显示：

```text
checkpointId/version
runId
currentStep
savedAt
status
```

## 禁止

```text
Spring Boot
Vue / React / Node
Multi-Agent
复杂分布式任务队列
生产级调度器
```

## 验收

至少证明：

1. 6 步任务开始执行；
2. 中途产生 checkpoint；
3. 受控中断；
4. Resume 从正确步骤继续；
5. Context 过大时出现一次可观察 Compaction；
6. Validator 能制造一次失败；
7. Evaluator feedback 驱动一次有限优化；
8. Web 页能看清上述过程；
9. 自动测试不需要真实在线 LLM。
