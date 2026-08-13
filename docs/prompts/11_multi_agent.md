# 实现 Prompt 11：Orchestrator + Workers

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/12_MultiAgent_Orchestrator与Handoff.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab11-multi-agent
```

实现：

```text
TaskStatsWorker
FailureAnalysisWorker
RecommendationWorker
Orchestrator
```

## 约束

- Worker 有各自独立 Context
- 可以共享同一个 LlmClient
- Worker 输入输出必须结构化
- Orchestrator 负责选择、调用、汇总
- 不允许 Agent 自由无限互聊
- 不允许递归创建新 Agent

## 对照

同时提供：

```text
SingleAgentReportGenerator
MultiAgentReportGenerator
```

打印比较：

```text
model_calls
context_chars
steps
success
```

README 分析什么时候 Multi-Agent 值得。
