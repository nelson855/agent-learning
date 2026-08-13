# lab05-planning

Planning 与 Plan-and-Replan：先用模型拆出结构化计划，再逐步执行；某一步失败时，才由模型重新规划并继续。

- 对应教材：`docs/chapters/05_Planning与PlanReplan.md`
- 对应 Prompt：`docs/prompts/05_planning_replan.md`
- 包名：`com.example.agentlearning.lab05`

## 1. 本模块学什么

理解 **ReAct 为什么还不够，Planning 补上什么**：

- ReAct 是"走一步看一步"——对复杂任务容易漏步骤、重复做、在局部问题上循环、看不到整体完成度；
- Planning 先给任务一个**全局结构**（目标 + 步骤 + 每步状态），执行时能看见完成度；
- 计划用**结构化数据**存（`Plan`/`PlanStep`/`PlanStepStatus`），而不是一段 Markdown——程序才能更新每步状态、定位失败步骤、把"已完成/失败"喂给 Replanner。

核心流程：

```text
Planner
  → Plan（目标 + 步骤，全部 PENDING）
  → Executor 逐步执行（RUNNING → DONE / FAILED）
  → 失败?
     → Replanner（基于原目标 + 当前计划状态 + 失败原因）→ NEW PLAN → 继续
  → 全部 DONE → FINAL STATUS
```

## 2. 为什么需要这个概念

Planner 和 Executor 可以共用同一个模型，只是 **Prompt 角色不同**（先"想清楚再动手"，再"动手"）。

一个危险点：**不要每执行一步都无条件 Replan**，否则成本快速上升（Planner 一次 + 每步 Replanner 一次 + Executor 一次）。
本模块只在**步骤失败**时 Replan，并用 `maxReplans` 加上限。

## 3. 代码结构

```text
数据结构
  PlanStepStatus    PENDING / RUNNING / DONE / FAILED / SKIPPED
  PlanStep          一步（状态可变，failureReason 记录失败原因）
  Plan              目标 + 步骤列表
  PlanParser        把模型输出的计划 JSON 解析成 Plan（确定性校验）

规划
  Planner           调模型生成初始计划
  Replanner         失败时调模型重新规划（输入原目标 + 计划状态 + 失败原因）

执行
  StepOutcome       一步的执行结果
  FakeLearningStepTool  模拟 completeLearningStep(stepId)；S2 第一次固定失败 DEPENDENCY_MISSING
  Executor          推 RUNNING、调工具、置 DONE/FAILED

编排
  PlanningRunner    Plan-and-Replan 主循环，打印 INITIAL PLAN / STEP RESULT /
                    REPLAN REASON / NEW PLAN / FINAL STATUS
  PlanRunResult     最终结果（replans 次数、是否 allDone、summary）
```

## 4. 如何运行

```bash
mvn -pl labs/lab05-planning test               # 确定性测试（不依赖真实模型）
mvn -pl labs/lab05-planning compile exec:java   # 离线剧本演示（结果确定，必触发 Replan）
mvn -pl labs/lab05-planning compile exec:java -Dexec.args="--real"   # 真实模型规划体验
```

> LLM 配置从环境变量或仓库根目录 `.env` 读取（`LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`）。
> 未配置真实 LLM 时，默认模式自动使用剧本，保证任何环境都能跑通。

## 5. 运行时应该观察什么

```text
INITIAL PLAN
  goal: 完成 Agent 学习第一阶段
  [S1] 阅读 Agent 基础概念  (PENDING)
  [S2] 动手实现一个 ReAct 循环  (PENDING)
  [S3] 完成学习总结与验收  (PENDING)

STEP RESULT: [S1] ... => DONE
STEP RESULT: [S2] ... => FAILED (DEPENDENCY_MISSING（第 1 次尝试）)

REPLAN REASON: 步骤 [S2] ... 执行失败: DEPENDENCY_MISSING（第 1 次尝试）

NEW PLAN
  [S1] 阅读 Agent 基础概念  (PENDING)
  [S2] 先补齐前置依赖，再动手实现 ReAct 循环  (PENDING)
  [S3] 完成学习总结与验收  (PENDING)

STEP RESULT: [S1] ... => DONE
STEP RESULT: [S2] ... => DONE
STEP RESULT: [S3] ... => DONE

FINAL STATUS: SUCCESS（3/3 DONE, replans=1）
```

重点观察：

1. **只失败时才 Replan**：S1/S3 成功时没有任何 Replanner 调用；只有 S2 失败才出现 `REPLAN REASON` 与 `NEW PLAN`；
2. **Replanner 输出了新计划**：S2 的 description 从"动手实现 ReAct"变成"先补齐前置依赖再实现"，是模型基于失败原因做的调整；
3. **成本可观测**：`FINAL STATUS` 里的 `replans=1` 对应一次额外的模型调用（用 `CountingLlmClient` 可在测试中断言 Planner 1 次 + Replanner 1 次 = 2 次）。

## 6. 本模块刻意没有实现什么

- **没有把每步的执行交给 ReAct Agent**：本模块 Executor 直接调工具；教材 5.8 的"Plan 大步骤 + 步骤内部 ReAct"是下一层组合，这里不做。
- **没有真实的 Plan 持久化 / Checkpoint**：Plan 只存在内存里，退出即消失。
- **没有依赖关系建模**：PlanStep 之间没有显式 DAG，只是顺序列表。
- **没有 Memory / RAG / Web UI / 多 Agent**：同前几章边界。

## 7. 测试

`PlanningRunnerTest`（全部用 `ScriptedLlmClient` 剧本 + `FakeLearningStepTool`，不依赖真实模型）覆盖：

- 正常步骤不触发 Replan（`replans=0`，模型只调 1 次）；
- 失败触发一次 Replan（`replans=1`，模型调 2 次）；
- max replans 有上限（反复失败 → 达到上限后剩余步骤 SKIPPED，模型调 1+3 次）；
- PlanParser 结构化解析（goal / steps / 初始状态 PENDING）；
- Replanner 正确携带失败上下文。
