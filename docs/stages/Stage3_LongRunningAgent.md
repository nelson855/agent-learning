# Stage 3 综合项目：Long-running Agent

## 前置

完成 RAG、Context Engineering、Compaction、Checkpoint、Evaluator。

## 项目目录

```text
stages/stage03-long-running-agent
```

## 目标

实现一个可以“长时间执行并恢复”的任务助手，并通过 Web 调试台直接观察长任务 Agent 的内部状态。

## 必须具备

```text
Knowledge Retrieval
Memory Retrieval
ContextBuilder
Compaction
Structured Summary
Checkpoint Versioning
Resume
Deterministic Validation
Evaluator Loop
```

## 综合任务

导入若干本地项目规范文档。

用户：

> 根据这些规范制定一个 6 步开发计划，每完成一步记录结果；如果中断，下次继续；最后生成一份符合规范的 JSON 总结。

执行过程中模拟中断，再 Resume。

## Web UI：Long-running Agent Debugger

页面重点不再只是聊天，而是“运行过程”。

### 至少展示

```text
Run Overview
- runId
- status
- current step

Plan
- step list
- completed / current / pending

Context Inspector
- RAG retrieved docs
- retrieved memories
- selected context items
- compacted summary（发生时）

Checkpoint Timeline
- checkpoint id/version
- savedAt
- currentStep
- resume target

Validation / Evaluator
- validator result
- evaluator feedback
```

### 必须提供的教学操作

- Start Run；
- Simulate Interruption；
- 查看 Checkpoint；
- Resume；
- 查看 Resume 后继续的 step；
- 查看最终 validator / evaluator 结果。

“模拟中断”使用可控教学机制，不要真的杀系统进程。

## Web 技术

继续使用：

```text
JDK HttpServer
HTML + CSS + 原生 JavaScript
```

不引入新的 Web Framework。

## API 原则

至少需要能：

```text
创建 run
读取 run 状态
读取 context snapshot
读取 checkpoint 列表
触发安全的教学中断
resume run
读取 evaluator / validation 结果
```

路径由实现自行设计，保持简单即可。

## 验收

必须能在页面或日志明确观察：

```text
RAG retrieved docs
memory retrieved items
context selected items
compaction
checkpoint save/load
resume point
validator
evaluator feedback
```

并证明核心逻辑在不启动 Web Server 时仍可通过自动测试。

## AI 编程工具使用方式

开始实现前读取：

```text
AGENTS.md
docs/04_Web可视化调试台规范.md
docs/stages/Stage3_LongRunningAgent.md
docs/prompts/stage03_long_running_agent.md
```

只修改本 Stage 模块，不回写历史 Lab / Stage。
