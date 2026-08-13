# 第 12 章：Multi-Agent——什么时候真的需要多个 Agent

## 12.1 先消除一个误解

Multi-Agent 不等于：

```text
创建 5 个不同 System Prompt
→ 让它们互相聊天
```

真正有价值的 Multi-Agent 一般需要：

- 明确职责
- 独立 Context
- 明确输入输出
- 协调机制
- 停止条件
- 共享或隔离状态规则

## 12.2 Orchestrator-Worker

例如任务：

> 生成任务系统运行分析。

Orchestrator 拆：

```text
Worker A：任务统计
Worker B：失败原因
Worker C：改进建议
```

最后：

```text
Orchestrator Merge
```

## 12.3 为什么独立 Context 有价值

一个 Agent 同时处理所有内容时：

```text
Context 越来越大
任务角色混杂
```

Worker 可以只看到自己需要的资料。

这是一种：

> Context 隔离。

## 12.4 Handoff

另一种模式：

```text
General Agent
↓
发现是数据库问题
↓
handoff to Database Agent
```

与 Orchestrator 的差别：

- Orchestrator 更像主动拆解/协调；
- Handoff 更像职责转移。

## 12.5 Multi-Agent 的成本

增加：

```text
模型调用次数
上下文复制
状态同步
失败组合
调试难度
评估难度
```

所以先问：

> 单 Agent + Tool 能不能解决？

## 12.6 本章 Demo

实现三个 Worker：

```text
TaskStatsWorker
FailureAnalysisWorker
RecommendationWorker
```

一个 Orchestrator：

```text
输入目标
→ 决定调用哪些 Worker
→ 收集结果
→ 汇总
```

第一版 Worker 可以共享同一个 `LlmClient`，但拥有不同 Prompt 和 Context。

不要做：

- Agent 之间自由聊天
- 无限递归创建子 Agent
- 动态创建任意角色

## 12.7 对照实验

Version A：

```text
一个 Agent 处理所有内容
```

Version B：

```text
Orchestrator + 3 Workers
```

比较：

- Context 大小
- 调用次数
- 输出稳定性
- 调试复杂度

---

## 本章自测

1. Multi-Agent 最核心的价值一定是“更聪明”吗？
2. Orchestrator-Worker 和 Handoff 有什么区别？
3. 为什么 Multi-Agent 常常更难调试？
4. 什么时候应该优先 Single Agent + Tools？

## 参考答案

1. 不一定，更常见价值是职责拆分、并行和 Context 隔离。
2. 前者强调一个协调者拆解和汇总，后者强调任务从一个 Agent 转交给另一个 Agent。
3. 因为调用、状态、上下文和失败路径成倍增加。
4. 当单 Agent 能清楚完成任务、Context 仍可控、工具职责明确时。
