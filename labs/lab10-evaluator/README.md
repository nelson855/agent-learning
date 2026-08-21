# lab10-evaluator

Evaluator + 确定性校验：从 SQLite 任务统计生成一份 JSON 周报。

- 模块名称：lab10-evaluator
- 学什么：Reflection、Evaluator 与 Generator-Evaluator Loop（结果验收与优化）
- 对应教材：`docs/chapters/11_Reflection_Evaluator与确定性校验.md`
- 对应 Prompt：`docs/prompts/10_evaluator_optimizer.md`

## 1. 本模块学什么

理解「生成 → 校验 → 评估 → 重试」的分工，以及**为什么不能只靠 LLM 互相检查**。

核心 Pipeline：

```text
SQLite 任务统计(TaskStats)
  ↓
Generator 生成周报JSON        ← LlmClient
  ↓
ProgramValidator 程序校验      ← 能由程序确定的判断，不调模型
  ↓ 通过
LlmEvaluator 语义评估           ← pass / score / issues
  ↓ Pass?
  ├─ Yes → 返回合格周报
  └─ No  → feedback 回灌 Generator 重试（最多 3 次）
```

## 2. 为什么需要这个概念

- **“再让模型检查一次”不是万能**：两次调用可能犯同一个错，成本翻倍，还可能无限互相否定。
- **分层**：凡 JSON 可解析、必填字段、数字类型、集合非空等**能由代码确定判断的，优先由程序判断**
  （第一层 ProgramValidator），只有涉及**语义**（summary 是否解释异常、建议是否可执行）才交给
  LLM（第二层 LlmEvaluator），并给出结构化 Rubric 反馈。
- **必须限次**：`GeneratorEvaluatorLoop.MAX_ITERATIONS = 3`，防止失控循环。

## 3. 如何运行

```bash
# 测试（确定性，FakeLlmClient，不依赖在线模型）
mvn test

# 离线 Demo（演示两个教学场景）
mvn exec:java -Dexec.mainClass=com.example.agentlearning.lab10.Main -Dexec.args=--demo

# 真实模型 Demo（需配置 LLM_BASE_URL / LLM_API_KEY / LLM_MODEL，可用仓库根目录 .env）
mvn exec:java -Dexec.mainClass=com.example.agentlearning.lab10.Main
```

## 4. 运行时应该观察什么

`--demo` 会有两个场景的日志：

**场景 A（结构错误）**：Generator 漏掉 `recommendations`。
程序校验直接拒收，日志出现 `PROGRAM VALIDATION FAILED`，且 **Evaluator 调用次数 = 0**
——结构问题不需要浪费一次模型判断。

**场景 B（语义差）**：结构合法但 summary 空洞、建议宽泛。
先 `PROGRAM VALIDATION PASSED` 才进入 Evaluator；评估 `REJECTED` 后把 issues 回灌给
Generator，重试生成一份合格周报，最终 `EVALUATOR PASSED`，Evaluator 恰好调用 2 次。

对照教材自测：

1. 为什么确定性验证优先于 LLM Judge？   → 稳定、便宜、可重复测试
2. Evaluator 什么时候才真有价值？         → 质量标准难以完全编码、需要语义判断时
3. 为什么必须限制 Loop 次数？             → 防止互相否定导致无限循环与失控成本
4. “请认真反思一下”为什么不是完整方案？   → 缺少明确检查对象、标准、反馈结构、终止条件

## 5. 刻意没有实现什么

- **没有实现 Reflection 的宽泛语义**：本模块只把“复盘”落成精确的
  `检查对象 / 检查标准 / 反馈结构 / 重试条件 / 最大次数`，不做“让模型自我反思”的玄学。
- **没有让 Evaluator 重新生成周报**：Evaluator 只返回 `pass/score/issues`，不产内容，
  内容仍由 Generator 依据反馈重新生成——职责分离。
- **没有真实模型兜底的重复调用逻辑**：真实模型输出不可控，本模块靠 ProgramValidator +
  Rubric 兜底，若 3 次仍未通过则明确判失败（`accepted=false`）。
- **不涉及 Multi-Agent / Skill / Guardrail**：那些属于后续 lab11~13。