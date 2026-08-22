# lab11-multi-agent

**Multi-Agent：Orchestrator + Workers 对照实验** —— 什么时候真的需要多个 Agent。

## 1. 本模块学什么

- **Single Agent**：一个 Agent 把全部上下文塞进一次调用，一次性输出所有内容；
- **Orchestrator-Worker 模式**：Orchestrator 决定调用哪些 Worker → 每个 Worker 用独立 Prompt + 独立 Context → 汇总。
- **对比维度**：model_calls、context_chars、steps、success。

## 2. 为什么需要这个实验

| 单 Agent | Multi-Agent |
|---|---|
| 1 次调用 | 3+ 次调用 |
| Context 包含全部数据 | 每个 Worker 只看自己需要的片段 |
| 职责混杂 | 职责明确 |
| 单点失败 | 可以局部重试 |

先问"单 Agent + Tool 能不能解决？"再考虑 Multi-Agent。

## 3. 项目结构

```text
src/main/java/com/example/agentlearning/lab11/
├── Main.java                     # CLI 入口：seed 数据 → 跑对比
├── Comparison.java               # 对照运行器：打印对比表
├── SingleAgentReportGenerator.java  # Version A：一次搞定
├── MultiAgentReportGenerator.java   # Version B：Orchestrator + 3 Workers
├── Orchestrator.java             # 选择 → 调用 → 合并
├── TaskStatsWorker.java          # Worker A：任务统计（看到全部统计）
├── FailureAnalysisWorker.java    # Worker B：失败原因（只看到失败/退化部分）
├── RecommendationWorker.java     # Worker C：改进建议（只看到异常概况）
├── ReportJson.java              # 结构化 JSON 解析工具
├── TaskStats.java / TaskRepository / Database  # SQLite 数据源
├── LlmClient / FakeLlmClient / ...            # LLM 基础设施
```

## 4. 如何运行

```bash
mvn -pl labs/lab11-multi-agent exec:java \
  -Dexec.mainClass=com.example.agentlearning.lab11.Main \
  -Dexec.args="data/lab11.db"

# 或只用测试（不需要真实 LLM）
mvn -pl labs/lab11-multi-agent test
```

## 5. 运行时应该观察什么

```text
===== Version A: Single Agent =====
  model_calls  = 1
  context_chars= 462
  success      = true

===== Version B: Multi-Agent (Orchestrator + 3 Workers) =====
  model_calls  = 3
  context_chars= 1032
  success      = true

===== 对比表 =====
方案       model_calls  context_chars  steps    success
Single    1           462            1        true
Multi     3           706            4        true
```

关键观察：

- **model_calls**：Multi 是 Single 的 3 倍 —— 成本更高；
- **context_chars**：虽然 Multi 的总体上下文更大（3 份独立的），但**每个 Worker 的上下文量远小于单 Agent**（TaskStatsWorker ≈ 230 chars vs 单 Agent 462 chars；FailureAnalysisWorker 仅 ≈180 chars）；
- **steps**：Multi 拆为 4 步（选择 + 3 Workers），可观测性更好；
- **success**：都产出完整报告。但如果某一 Worker 失败，Multi 可以只重试那一 Worker。

## 6. 哪些能力刻意没有实现

- **Handoff**：本实验只演示 Orchestrator-Worker，未演示职责转移；见教材 12.4
- **递归创建 Agent**：不允许
- **Agent 之间自由聊天**：不允许
- **真实模型**：脚本化 FakeLlmClient 带来的确定性输出，不依赖真实 LLM