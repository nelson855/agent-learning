# 第 15 章：Observability、Tracing 与 Agent Evaluation

## 15.1 为什么传统日志不够

普通服务错误：

```text
接口失败
SQL 异常
```

Agent 错误可能是：

```text
模型先误选 Tool
→ Tool Result 又影响下一轮
→ Planner 改了错误计划
→ 最终答案看起来仍然很合理
```

所以必须能看到完整轨迹。

## 15.2 Trace

一次 Run：

```text
RUN
├─ Model Call 1
├─ Tool Call 1
├─ Tool Result 1
├─ Model Call 2
├─ Tool Call 2
└─ Final
```

每个 Span 保存：

```text
type
start
end
status
input summary
output summary
token/cost if available
error
```

学习阶段可以全部存 SQLite。

## 15.3 不要只看 Final Answer

一个 Agent 最终回答正确，过程可能非常差：

```text
调用 15 次工具
其中 8 次错误
最后碰巧答对
```

所以评估至少分：

```text
Outcome
Process
Cost
Reliability
```

## 15.4 Evaluation Dataset

准备固定测试集：

```json
{
  "caseId": "...",
  "input": "...",
  "expected": {
      "mustUseTools": ["getTask"],
      "forbiddenTools": ["deleteTask"],
      "finalContains": ["OPEN"]
  }
}
```

运行 20 次。

记录：

```text
success rate
avg steps
tool error rate
max step exceeded
```

## 15.5 LLM-as-Judge

只能用于难以代码化的指标，例如：

```text
回答是否清晰
建议是否可执行
总结是否覆盖主要问题
```

最好：

- 结构化 Rubric
- 多样测试样本
- 与确定性指标分开

## 15.6 本章 Demo

实现：

```text
TraceRepository
EvaluationRunner
```

准备 5~10 个用例。

FakeLlmClient 做确定性单元测试。

真实模型做手工实验。

输出简单控制台报告：

```text
TOTAL
SUCCESS
AVG_STEPS
TOOL_ERRORS
MAX_STEP_EXCEEDED
```

## 15.7 到这里你已经在写 Harness

当系统拥有：

```text
Loop
Tools
State
Memory
Context
Checkpoint
Guardrail
Trace
Evaluation
```

它已经不再只是一个“调用 LLM 的应用”。

你实际上已经手写出了一个简化 Agent Harness。

---

## 本章自测

1. 为什么 Agent 不能只记录最终答案？
2. Trace 和普通业务日志有什么不同？
3. 为什么 Agent Eval 要同时看结果和过程？
4. LLM-as-Judge 最适合判断什么？

## 参考答案

1. 错误可能发生在多步决策链中，最终答案无法解释过程质量和成本。
2. Trace 强调一次 Agent Run 内模型、工具、状态变化之间的因果轨迹。
3. 最终正确不代表路径可靠、便宜或安全。
4. 难以通过确定性程序完全表达的语义质量。
