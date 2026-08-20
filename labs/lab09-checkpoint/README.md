# lab09-checkpoint

Agent Harness 与 Checkpoint：多步任务执行到一半崩溃，能从 Checkpoint 恢复并跳过已完成的步骤。

对应教材：`docs/chapters/10_Harness_Checkpoint与长任务.md`
对应 Prompt：`docs/prompts/09_harness_checkpoint.md`

## 1. 本模块学什么

把一个人工 Agent 的"运行底座"拆成几个明确组件，重点观察 **Checkpoint / Resume**：

```text
AgentRunner          # 核心循环：逐 step 执行，每步推进后落一次检查点
AgentState           # 可恢复的运行状态（runId / goal / plan / currentStepIndex / 各步结果）
CheckpointRepository # SQLite 存取，每次保存【新 version】，不覆盖历史
ContextBuilder       # 从 state 现组装"恢复上下文"（不把整套 Context 存进 checkpoint）
ToolRegistry         # 按名字注册/查找工具，Agent 步骤以工具名驱动行为
```

关键观察点是"Checkpoint 不是 History"：

| 概念 | 记录什么 | 作用 |
| --- | --- | --- |
| Conversation History | 聊天过程 | 给人看"谈过什么" |
| Checkpoint | 能恢复执行的运行状态 | 给系统看"从哪继续"，崩了能续 |

以及"为什么保留 version"：可以看状态演化、回滚、调试，也避免覆盖后丢失事故现场。

## 2. 为什么需要 Checkpoint

```
S1 DONE → S2 DONE → S3 RUNNING → 【JVM 崩溃】
```

没有 Checkpoint：从头再来，S1、S2 白做。
有 Checkpoint：

```text
load latest checkpoint
→ 恢复 AgentState
→ 从 S3 继续（S1、S2 不重复执行）
```

长任务不能只靠"更大上下文窗口"（那是把 maxSteps 从 8 改成 800）。
真正的长任务必须组合 State + Checkpoint + Compaction + 外部进度 + Retry + Budget + Evaluation。

## 3. 如何运行

```bash
# 1) 跑全部测试（不依赖真实模型，纯确定性）
mvn test

# 2) 离线演示：5 步任务 → 第 3 步前模拟崩溃 → 从最新 Checkpoint 恢复并跑完
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab09.Main -Dexec.args="--demo"

# 3) 交互模式：/start 建任务并在第 3 步崩溃 → 重启本程序 → /resume runId 恢复
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab09.Main
```

命令：`/start` 创建 5 步任务并推进到第 3 步前崩溃（打印 RUN_ID）；退出后重新启动，`/resume runId` 从最新 Checkpoint 恢复执行到完成；`/exit` 退出。

> 说明：本 Lab 的过程/步骤是靠**确定性脚本计划**驱动的（不依赖 LLM）。
> 目的是让"哪一步被跳过、哪一步被重做"可以被精确断言，而不是每次跑结果都变。
> 若要观察 LLM 驱动的 Plan，见 stage03 阶段综合项目。

## 4. 运行时应该观察什么

`--demo` 会依次打印：

1. `[STEP 1..n] sX → 工具结果`——每步推进行为；
2. `CHECKPOINT SAVED version=x`——每完成一步就落一个新 version（不覆盖）；
3. `SIMULATED CRASH`——在第 3 步执行前中断，模拟进程崩溃；
4. `LOADED CHECKPOINT version=x`、`RESUME FROM step=y`——重启后加载最新 checkpoint；
5. 恢复上下文（ContextBuilder 现组装）：GOAL、PLAN（DONE/RUNNING/PENDING 标记）、NEXT_STEP_INDEX、STEP_RESULTS；
6. 继续执行到全部完成，并打印"共保存检查点 N 个版本"。

验收输出（Prompt）形如：

```text
CHECKPOINT SAVED version=x
SIMULATED CRASH
LOADED CHECKPOINT version=x
RESUME FROM step=y
```

测试里 `AgentRunnerTest` 用**每步调用计数**精确验证：崩溃 + 恢复后，已 DONE 的步骤只执行 1 次、未完成步骤被继续做掉。

## 5. 本模块刻意没有实现什么

- **真实 LLM 驱动的 Plan**：这里是确定性脚本计划，方便可复现地观察 checkpoint 机制本身。
- **捕获任意真实异常（catch 后原地继续）**：`SimulatedCrashException` 是显式抛出模拟崩溃；真实容错/重试场景属于更靠后的阶段综合。
- **进程级崩溃恢复（真正 kill -9 后重启）**：本 Lab 在单进程内模拟"崩溃 → 重新构造 Runner → 恢复"，验证的是状态能否从 SQLite 读回，而非操作系统级恢复。
- **Context Compaction 与 Checkpoint 联动**：压缩见 lab08；两者组合留到 stage03。
- **Checkpoint 的目录/对象存储**：只用 SQLite 单表，简单可读。
- **Web UI**：本 Lab 是 CLI。
