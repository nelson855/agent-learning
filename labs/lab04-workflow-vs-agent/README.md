# lab04-workflow-vs-agent

同一个需求，用 **固定 Workflow** 和 **自主 Agent** 各实现一遍，运行同一个入口，打印两版的 `model_call_count / tool_call_count / step_count / success` 逐项对照。

- 对应教材：`docs/chapters/04_WorkflowPatterns.md`
- 对应 Prompt：`docs/prompts/04_workflow_comparison.md`
- 包名：`com.example.agentlearning.lab04`

## 1. 本模块学什么

理解 **Workflow 与 Agent 的本质区别**：

- **Workflow（Version A）**：执行路径（先做什么、再做什么、怎么收尾）**由程序代码预定**。模型只是流程里被固定插入的"产出点"，不参与路径决策。
- **Agent（Version B）**：执行路径**由模型根据当前上下文动态决定**。模型每一步决定调用哪个工具、传什么参数，直到它自己认为任务完成（输出 `final`）。

两版面对的是**同一个需求**：根据用户给的主题，生成一个任务标题和一段描述，校验后保存到 SQLite。

| 维度 | Version A Workflow | Version B Agent |
|---|---|---|
| 路径决定者 | 程序（`TaskWorkflow.run` 写死顺序） | 模型（`AgentRunner` 循环里逐步决策） |
| 模型调用次数 | 固定 = 2（生成标题 + 生成描述） | 不确定（本演示 4 次决策 + 2 次工具内生成） |
| 工具调用 | 0 次（生成直接用模型，保存走方法调用） | 3 次（generateTitle / generateDescription / saveTask） |
| 校验 | `TaskRules.validate` 确定性校验 | 同一份 `TaskRules`，由 saveTask 工具调用 |
| 失败处理 | 程序中断，返回失败结果 | 工具失败作为 Observation 交回模型，模型决定下一步 |

## 2. 为什么需要这个概念

初学者容易混淆"用了 LLM + 工具 = Agent"。这个实验用**同一个需求的两个实现**把区别摆到桌面上：

- Workflow 里也有模型、也产生智能输出，但**路径是写死的**，不可观测到"模型在做决策"；
- Agent 里模型不只是产出内容，还**决定调用哪个工具、以及下一步做什么**；
- 关键结论：**确定性留给程序，不确定性判断交给模型**。两版共用 `TaskRules`（怎么算合法、id 怎么生成、怎么落库）就是为此——校验这种能确定算出来的事，永远不让模型自由发挥。

## 3. 如何运行

```bash
mvn -pl labs/lab04-workflow-vs-agent test        # 确定性测试（不依赖真实模型）
mvn -pl labs/lab04-workflow-vs-agent exec:java    # 交互式 CLI（见下方说明）
```

> `exec:java` 需根 `pom.xml` 配置了 `exec-maven-plugin`；也可以直接在 IDE 中运行 `Main.main()`。

LLM 配置从环境变量或仓库根目录 `.env` 读取：

```text
LLM_BASE_URL
LLM_API_KEY
LLM_MODEL
```

- 已配置 → 真实模型演示（`Main` 打印 `（真实模型）`）。
- 未配置 → 自动退化为**离线剧本演示**（`ScriptedLlmClient` 固定回复），任何环境都能跑通并看到对比结构。

CLI 交互：输入一个任务主题回车，同一主题会先用 Workflow、再用 Agent 各执行一遍并打印对比；输入 `/exit` 退出。任务落在 `./data/lab04-workflow-vs-agent.db`。

## 4. 运行时应该观察什么

对同一个主题，逐行对照两版输出：

1. **Version A（Workflow）**
   - `执行步骤` 永远是同一串：`generateTitle → generateDescription → validate → save`，和输入主题无关；
   - `model_call_count = 2` 固定不变，`tool_call_count = 0`；
   - 如果标题/描述不合法，`success = false` 且输出校验错误（说明校验由程序完成）。
2. **Version B（Agent）**
   - 打印 `STEP 1/2/3...`，每步有 `ACTION TYPE / TOOL NAME / TOOL RESULT`；
   - `tool_call_count` 反映模型实际调了 3 次工具；`model_call_count` 比 Workflow 高（4 次决策 + 2 次工具内部生成）；
   - 模型会先生成标题、再基于标题生成描述、最后保存——顺序是**模型自己走出来的**，不是代码规定的。

重点观察：**同一需求，Agent 的模型调用次数更多、步骤不可预测**，这就是"智能"换来的成本。

## 5. 稳定性 / 灵活性 / 可测试性 / 可预测成本

Prompt 要求的四个对比问题：

| 问题 | Workflow | Agent |
|---|---|---|
| **稳定性** | 高。路径固定，输入相同基本输出相同，模型只插在两个固定产出点 | 低。路径由模型决定，同样的输入可能走不同步骤，甚至跑偏 |
| **灵活性** | 低。需求一变化（比如要先查已有任务再生成），就得改代码 | 高。加一个工具、改一下系统提示，模型就可能自己组合出新路径 |
| **可测试性** | 高。2 次固定模型调用，剧本写两条就能 100% 确定结果 | 中。可测试但更费力：要按真实决策顺序排剧本，路径一变测试就要改 |
| **可预测成本** | 高。模型调用次数固定（本模块恒为 2） | 低。调用次数不确定，受 maxSteps 上限保护（默认 8） |

工程结论：**能写死、该写死的，用 Workflow；路径本身不确定、需要模型临场判断的，才用 Agent。**

## 6. 代码结构

```text
Version A（固定 Workflow）
  TaskWorkflow         执行器：固定 generateTitle → generateDescription → validate → save
  WorkflowResult       一次运行结果（success / failureReason / task / steps）

Version B（Agent）
  AgentRunner          ReAct 最小闭环：Context → 模型决策 → 工具执行 → Observation
  AgentTools           三个工具：generateTitle / generateDescription / saveTask
                       （前两个内部还会再调一次模型来产出内容）
  AgentDecision        一次决策（TOOL_CALL / FINAL + decisionSummary）
  AgentContext         不断增长的消息历史，工具结果以 [observation] 前缀追加
  StepTrace / AgentRun 步骤轨迹与运行结果

两版共用（确定性留给程序）
  TaskRules            标题/描述校验规则 + 任务 id 生成 + 落库
  Task / TaskStore     SQLite 存储（JDBC）
  Tool* / ToolRegistry 工具体系；CountingLlmClient 统一统计 model_call_count
```

## 7. 本模块刻意没有实现什么

- **没有 Planner / Plan 可视化**：Agent 版不做"先规划再执行"，就是一步一看的 ReAct 最小闭环。
- **没有 Memory / RAG**：上下文就是对话历史 + `[observation]`，没有持久化记忆或检索。
- **没有 Checkpoint / 断点续跑**：失败就失败，不会恢复。
- **没有多 Agent 协作**：只有一个 Agent。
- **没有把校验交给模型**：标题/描述合法性始终由 `TaskRules` 确定性判断，Agent 只是发起保存调用。
