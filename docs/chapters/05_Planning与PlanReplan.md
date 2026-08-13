# 第 5 章：Planning、Task Decomposition 与 Plan-and-Replan

## 5.1 为什么 ReAct 还不够

ReAct 很像：

> 走一步看一步。

对于短任务很好。

但复杂任务如果完全没有计划，容易：

- 漏步骤
- 重复做
- 在局部问题上循环
- 看不到整体完成度

## 5.2 Task Decomposition

输入：

```text
分析最近的任务执行情况，并给出三个优化建议。
```

Planner 可以拆：

```text
1. 统计任务总量
2. 统计成功率
3. 找到失败最多的类型
4. 获取失败原因
5. 生成优化建议
```

任务分解本身就是一种 LLM 能力。

## 5.3 Plan 数据结构

学习阶段不要把 Plan 只保存成一段 Markdown。

建议结构化：

```json
{
  "goal": "...",
  "steps": [
    {
      "id": "S1",
      "description": "...",
      "status": "PENDING"
    }
  ]
}
```

状态：

```text
PENDING
RUNNING
DONE
FAILED
SKIPPED
```

## 5.4 Plan-and-Execute

```text
Planner
 ↓
Plan
 ↓
Executor 执行 Step 1
 ↓
Step 2
 ↓
...
```

这里 Planner 和 Executor 可以用同一个模型，只是 Prompt 角色不同。

## 5.5 Replan

如果某一步：

```text
查询任务统计
```

返回：

```text
数据表为空
```

原计划可能已经不适用。

Replanner 接收：

```text
原目标
原 Plan
已完成步骤
失败原因
当前环境信息
```

输出：

```text
保留哪些步骤
修改哪些步骤
新增哪些步骤
取消哪些步骤
```

## 5.6 一个危险点

不要每执行一步都无条件 Replan。

否则：

```text
Planner 调一次
每一步 Replanner 调一次
Executor 又调一次
```

成本会快速上升。

更合理：

```text
失败
环境发生重要变化
计划明显不完整
达到特定检查点
```

时才 Replan。

## 5.7 本章 Demo

目标：

> 为“完成 Agent 学习第一阶段”创建一个三步任务计划，并逐项模拟执行；第二步故意失败，然后触发一次 Replan。

先使用 Fake Tool：

```text
completeLearningStep(stepId)
```

第二步固定返回：

```text
DEPENDENCY_MISSING
```

观察：

```text
Initial Plan
→ Execute
→ Failure
→ Replan
→ New Plan
→ Continue
```

## 5.8 ReAct vs Plan-and-Replan

可以先这样记：

```text
ReAct：局部动态决策
Planning：全局结构
```

它们不是互斥。

Planner 可以规划大步骤，每个 Step 内部又由 ReAct Agent 自主执行。

---

## 本章自测

1. Planning 主要解决 ReAct 的什么问题？
2. 为什么 Plan 最好结构化存储？
3. Replan 应该什么时候触发？
4. Plan-and-Replan 和 ReAct 能否组合？

## 参考答案

1. 给复杂任务提供全局结构、完成度和依赖关系，降低走一步看一步造成的遗漏和循环。
2. 便于程序更新状态、持久化、恢复和验证。
3. 失败、环境显著变化、计划失效或明确检查点时。
4. 可以。常见方式是计划大步骤，单个步骤内部再使用 ReAct。
