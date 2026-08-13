# 实现 Prompt 05：Planning + Plan-and-Replan

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

阅读：

- `AGENTS.md`
- `docs/chapters/05_Planning与PlanReplan.md`

本次只实现/修改模块：

```text
labs/lab05-planning
```

## 实现

数据结构：

```text
Plan
PlanStep
PlanStepStatus
```

状态至少：

```text
PENDING
RUNNING
DONE
FAILED
SKIPPED
```

流程：

```text
Planner
→ Plan
→ Executor
→ Failure?
→ Replanner
→ Continue
```

## Demo

目标：

> 完成 Agent 学习第一阶段。

初始计划至少 3 步。

让第 2 步的 Fake Tool 第一次执行固定失败：

```text
DEPENDENCY_MISSING
```

只在失败时触发 Replan。

## 验收

打印：

```text
INITIAL PLAN
STEP RESULT
REPLAN REASON
NEW PLAN
FINAL STATUS
```

测试确保：

- 正常步骤不触发 Replan
- 失败会触发一次
- max replans 有上限
