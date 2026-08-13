# 第 11 章：Reflection、Evaluator 与 Deterministic Validation

## 11.1 “再让模型检查一次”不是万能方案

常见做法：

```text
LLM 生成
→ LLM 检查
→ LLM 修改
```

它可能有帮助，但存在问题：

- 两次调用可能犯同一个错
- 没有真实标准时，Evaluator 也只是意见
- 成本增加
- 容易无限优化

## 11.2 第一原则

> 能由程序确定判断的，优先由程序判断。

例如：

```text
JSON 是否合法 → JSON parser
SQL 是否执行成功 → SQLite
任务 ID 是否存在 → DB query
代码是否通过 → test
必填字段是否齐 → validator
```

这些不需要 LLM-as-Judge。

## 11.3 Evaluator 适合什么

例如：

> “这份任务总结是否覆盖失败原因、影响和建议？”

这种质量标准很难完全用代码判断。

可以定义 Rubric：

```text
完整性 0~2
证据 0~2
可执行性 0~2
```

Evaluator 返回结构化反馈：

```json
{
  "pass": false,
  "score": 4,
  "issues": [
    "没有说明失败原因"
  ]
}
```

## 11.4 Generator-Evaluator Loop

```text
Generate
↓
Deterministic Validate
↓
Evaluator
↓
Pass?
├─ Yes
└─ No → Feedback → Generate Again
```

必须有：

```text
maxIterations
```

## 11.5 本章 Demo

任务：

> 根据 SQLite 任务统计生成一份 JSON 周报。

第一层程序验证：

- JSON 可解析
- 必须字段存在
- 数字类型正确

第二层 LLM Evaluator 判断：

- 是否解释异常
- 是否有可执行建议

第一次故意让 Fake Generator 漏掉 `recommendations`。

观察：

```text
PROGRAM VALIDATION FAILED
```

根本不需要调用 Evaluator。

第二次结构正确但内容差，再进入 LLM Evaluator。

## 11.6 Reflection

Reflection 可以理解成更宽泛的：

> Agent 对自己的输出或执行过程进行复盘。

但在工程上应尽量落成：

```text
检查对象
检查标准
反馈结构
重试条件
最大次数
```

而不是一句：

```text
请你认真反思。
```

---

## 本章自测

1. 为什么确定性验证优先于 LLM Judge？
2. Evaluator 什么时候才有真正价值？
3. 为什么 Generator-Evaluator Loop 必须限制次数？
4. “请认真反思一下”为什么不是完整工程方案？

## 参考答案

1. 程序判断更稳定、便宜、可重复测试。
2. 当质量标准难以完全编码、需要语义判断时。
3. 防止模型互相否定导致无限循环和失控成本。
4. 缺少明确检查对象、标准、反馈结构和终止条件。
