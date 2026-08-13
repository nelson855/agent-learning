# 实现 Prompt 09：Harness + Checkpoint + Resume

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/10_Harness_Checkpoint与长任务.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab09-checkpoint
```

## 目标

把以下组件清楚拆开：

```text
AgentRunner
AgentState
CheckpointRepository
ContextBuilder
ToolRegistry
```

SQLite：

```text
agent_checkpoint
```

每次保存新 version，不覆盖历史。

## Demo

构造 5 步任务。

运行到第 3 步允许：

```text
/crash
```

模拟异常退出。

重新启动：

```text
/resume <runId>
```

从最新 Checkpoint 恢复。

## 验收

输出：

```text
CHECKPOINT SAVED version=x
SIMULATED CRASH
LOADED CHECKPOINT version=x
RESUME FROM step=y
```

测试：

- latest version 正确
- 恢复状态正确
- 已 DONE 步骤不会重复执行
