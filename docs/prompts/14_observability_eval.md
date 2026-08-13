# 实现 Prompt 14：Tracing + Evaluation Runner

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/15_Observability_Tracing与Evaluation.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab14-observability-eval
```

## Trace

SQLite 保存：

```text
run
span
```

span 类型至少：

```text
MODEL
TOOL
STATE
EVALUATOR
```

## Evaluation

创建 5~10 个固定测试案例。

每个 case 支持：

```text
input
expected tools
forbidden tools
expected final keywords
max steps
```

输出：

```text
TOTAL
SUCCESS
AVG_STEPS
TOOL_ERRORS
MAX_STEP_EXCEEDED
```

测试使用 FakeLlmClient。

真实模型运行作为可选手工实验，不得成为 Maven Test 的必要条件。
