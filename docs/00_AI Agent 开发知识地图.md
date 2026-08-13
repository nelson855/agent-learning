# AI Agent 开发知识地图

> 目标：面向具有后端开发经验、已经使用过大模型 API，但缺少 Agent 系统性知识的工程师。
>
> 学习重点不是模型训练，而是理解：**如何把一个 LLM 逐渐构造成能够自主完成复杂任务的 Agent 系统。**

---

# 一、先建立一个整体心智模型

可以把一个 AI Agent 想象成一个刚入职的软件工程师。

| AI Agent 世界 | 类比 |
|---|---|
| LLM | 工程师的大脑 |
| System Prompt | 岗位职责、工作规范 |
| User Prompt | 当前收到的任务 |
| Context | 当前桌面上摊开的所有资料 |
| Tool | IDE、浏览器、数据库、Shell 等工具 |
| Tool Calling | 工程师决定使用某个工具 |
| Memory | 笔记本、工作记录、个人经验 |
| RAG | 去资料库临时查资料 |
| ReAct | 想一下 → 动手 → 看结果 → 再想 |
| Plan | 开工前先列任务清单 |
| Replan | 执行过程中发现情况不对，重新排计划 |
| Agent Loop | 不断“思考—行动—观察”的工作循环 |
| Harness | 公司给工程师配的办公环境、规章制度、任务系统、权限系统 |
| Multi-Agent | 一个项目组里的多个工程师 |
| Orchestrator | 项目经理 / Tech Lead |
| Context Compression | 桌面资料太多，把旧资料整理成摘要 |
| Checkpoint | 工作做到一半保存现场 |
| Guardrail | 权限控制、安全制度 |
| Evaluation | 验收、测试、Code Review |

真正的 Agent，本质上并没有想象中那么神秘。

很多 Agent 最核心的结构其实就是：

```text
while (任务没有完成) {

    把当前任务 + 上下文 + 工具信息发送给 LLM

    LLM 决定：
        1. 直接回答
        2. 调用某个 Tool

    如果调用 Tool：
        执行 Tool

    把 Tool Result 放回 Context

}
```

在这个最简单的循环之上，再逐渐增加：

```text
Planning
Memory
Context Management
Evaluation
Checkpoint
Multi-Agent
Guardrail
...
```

就逐渐形成今天看到的各种 Agent Framework。

工程实践中也经常区分 **Workflow** 和 **Agent**：

- Workflow：程序提前决定流程，LLM 在流程里的某些节点工作；
- Agent：LLM 可以根据当前状态动态决定下一步做什么。

因此学习 Agent 时，最重要的不是先掌握某个框架，而是先理解这些底层模式。

---

# 二、第一阶段：LLM 应用基础

难度：⭐

这一阶段很多内容你可能已经接触过，但需要重新从“Agent”的角度理解。

## 1. LLM API

掌握程度：了解 + 简单 Demo

需要理解：

- Message
- System / User / Assistant
- Token
- Context Window
- Sampling
- Streaming
- 模型输入和输出

重点不是学习怎么调用 SDK，而是理解：

> **LLM 本身实际上是一个无状态函数。**

可以粗略理解成：

```text
response = LLM(context)
```

所谓“聊天”“记忆”“Agent”，大量能力其实都是外围程序构建出来的。

---

## 2. Prompt Engineering

掌握程度：实践

理解：

- System Prompt
- Few-shot
- Role
- Constraint
- Output Format
- Prompt Template

Agent 系统中的 Prompt 不只是：

```text
帮我回答问题
```

而更像是：

```text
你是谁
你的目标是什么
你有哪些工具
什么时候使用工具
有哪些禁止行为
任务完成条件是什么
发生错误怎么办
```

---

## 3. Structured Output

掌握程度：实践 + Demo

让模型不要返回：

```text
我认为应该查询一下订单……
```

而是返回类似：

```json
{
  "action": "QUERY_ORDER",
  "orderId": "123"
}
```

这是从：

> “让 AI 写文字”

走向：

> “让 AI 驱动程序”

非常关键的一步。

---

# 三、第二阶段：让 LLM 获得行动能力

难度：⭐⭐

---

## 4. Tool / Function Calling

掌握程度：重点实践 + Demo

这是 Agent 开发最重要的基础能力之一。

例如给模型三个工具：

```text
getWeather()
queryDatabase()
sendEmail()
```

用户：

```text
帮我看看北京天气
```

模型不直接编天气，而是产生类似：

```text
tool = getWeather
arguments = {
    city: "北京"
}
```

你的 Java 程序：

```text
LLM
 ↓
ToolCall
 ↓
Java Method
 ↓
Tool Result
 ↓
LLM
```

这一步是理解 Agent 的第一道真正分水岭。

---

## 5. Tool Design

掌握程度：实践

需要理解：

- Tool Name
- Tool Description
- Parameters
- Schema
- Tool Result
- Tool Error
- Tool Permission

Agent 是否聪明，很大程度上与：

> **你给它什么工具，以及工具设计得是否清晰**

有关。

---

## 6. Agent Loop

掌握程度：核心实践 + Demo

第一次真正手写 Agent。

核心：

```text
User
 ↓
LLM
 ↓
Tool Call?
 ├─ No → Final Answer
 └─ Yes
      ↓
    Execute Tool
      ↓
    Observation
      ↓
    LLM
      ↓
    ...
```

理解这个循环之后，再看 LangChain、LangGraph、Claude Agent SDK 等框架，就不会觉得它们很神秘。

---

# 四、第三阶段：Agent 的思考与执行模式

难度：⭐⭐⭐

---

## 7. ReAct

掌握程度：核心概念 + Demo

ReAct = Reasoning + Acting。

它的核心思想是让：

```text
Reason
Action
Observation
Reason
Action
Observation
...
```

交错进行。

ReAct 原始论文的核心就是把 reasoning 与 action 组合起来，让模型能够根据外部环境返回的信息继续调整后续行为。

可以简单理解：

```text
我要查订单 123

Thought:
首先需要查询订单。

Action:
queryOrder(123)

Observation:
订单状态：已发货

Thought:
用户可能还想知道物流情况，需要查询物流。

Action:
queryExpress(...)

Observation:
正在上海转运中心

Final:
你的订单已经发货，目前正在上海转运中心。
```

现在很多 Tool-Using Agent，本质上都能看到 ReAct 思想的影子。

---

## 8. Workflow

掌握程度：理解 + 小 Demo

Agent 不等于所有事情都让 AI 自己决定。

常见 Workflow：

### Prompt Chaining

```text
生成大纲
 ↓
检查大纲
 ↓
生成文章
```

### Routing

```text
用户请求
 ↓
分类
 ├─ 技术问题
 ├─ 财务问题
 └─ 客服问题
```

### Parallelization

```text
         ┌→ Agent A
任务 ────┼→ Agent B
         └→ Agent C
              ↓
             汇总
```

这些都是非常重要的 Agentic Workflow 模式。

---

# 五、第四阶段：让 Agent 学会规划

难度：⭐⭐⭐

---

## 9. Task Decomposition

掌握程度：实践 + Demo

例如：

```text
帮我分析特斯拉最近的经营情况
```

不能一次完成。

Agent 可以拆成：

```text
1. 搜集财务数据
2. 搜集销量数据
3. 搜集相关新闻
4. 分析财务情况
5. 分析竞争格局
6. 汇总结论
```

这就是任务分解。

---

## 10. Plan-and-Execute

掌握程度：重点实践 + Demo

不同于 ReAct 的：

```text
想一步
做一步
再想一步
```

Plan-and-Execute 更像：

```text
先制定完整计划

Plan:
1
2
3
4

然后逐项执行。
```

---

## 11. Plan-and-Replan

掌握程度：重点实践 + Demo

执行计划的时候，现实环境可能发生变化。

例如：

```text
Plan

1 搜索财报
2 获取竞争对手数据
3 对比分析
4 输出报告
```

执行第 2 步发现：

```text
竞争对手数据不足
```

于是：

```text
Replan

1 搜索行业报告
2 搜索公司公告
3 获取竞争对手数据
4 继续分析
```

核心思想：

```text
Plan
 ↓
Execute
 ↓
Observe
 ↓
Replan
 ↓
Execute
```

Plan-and-Replan 更适合长任务和不确定任务。

它更应该理解成一种 **Agent 工程模式**，而不是某个唯一、固定的标准协议。

---

# 六、第五阶段：多轮对话与 State

难度：⭐⭐⭐

---

## 12. Multi-turn Conversation

掌握程度：核心实践 + Demo

这是非常容易产生误解的地方。

模型通常不会真的：

> “记得上一句话。”

应用程序实际上是在不断重新发送：

```text
System
User 1
Assistant 1
User 2
Assistant 2
User 3
```

于是模型看起来像：

> “拥有连续对话能力。”

因此需要理解：

```text
Conversation
Session
Message History
State
```

之间的关系。

---

## 13. Agent State

掌握程度：重点实践 + Demo

相比普通聊天，Agent 的 State 更多。

例如：

```json
{
  "conversationId": "123",
  "goal": "...",
  "messages": [],
  "plan": [],
  "currentStep": 3,
  "toolResults": [],
  "status": "RUNNING"
}
```

Agent 本质上可以理解成：

```text
State
 ↓
LLM / Tool
 ↓
New State
 ↓
LLM / Tool
 ↓
New State
```

这也是理解 LangGraph 一类“Graph + State”框架的重要前置知识。

---

# 七、第六阶段：Memory

难度：⭐⭐⭐⭐

这是 Agent 开发里最容易混乱的概念之一。

---

## 14. Short-Term Memory

掌握程度：实践 + Demo

主要解决：

> 当前这次任务中发生了什么？

例如：

```text
Message History
当前 Plan
Tool Result
当前任务进度
```

通常和当前 Session / Thread 绑定。

---

## 15. Long-Term Memory

掌握程度：重点实践 + Demo

解决：

> 跨 Session 以后，我还要记住什么？

例如：

```text
用户喜欢 Java
用户习惯 Maven
某项目使用 JDK 21
```

可以存入：

```text
MySQL
Redis
Vector Database
Document Store
```

然后在未来需要时再取出来。

现代 Agent memory 系统也往往区分“记忆提取”和“记忆存储/检索”，而不是简单地把完整聊天记录永久塞进 Prompt。

---

## 16. Memory Retrieval

掌握程度：实践 + Demo

有记忆不代表：

```text
每次全部塞给 LLM。
```

而是：

```text
当前任务
 ↓
判断需要哪些 Memory
 ↓
Retrieve
 ↓
加入 Context
```

因此这里会涉及：

```text
Keyword Search
Metadata Filter
Vector Search
Semantic Search
```

---

# 八、第七阶段：RAG、Memory 与 Context 的区别

难度：⭐⭐⭐⭐

## 17. RAG

掌握程度：重点理解 + Demo

RAG：

```text
问题
 ↓
搜索外部知识
 ↓
返回相关内容
 ↓
放入 Context
 ↓
LLM
```

例如：

```text
公司的请假制度是什么？
```

去知识库查询。

---

## 18. RAG vs Memory

必须彻底区分。

### RAG

解决：

> “世界上 / 知识库里有什么信息？”

例如：

```text
公司制度
产品文档
代码
研究报告
```

### Memory

解决：

> “这个 Agent 以前经历过什么？”

例如：

```text
用户习惯
历史决策
过去任务结果
```

二者底层都可能使用 Vector Database。

但是：

> **存储技术相似 ≠ 概念相同。**

---

# 九、第八阶段：Context Engineering

难度：⭐⭐⭐⭐

这是现代 Agent 开发非常重要的一层。

Prompt Engineering 更关心：

```text
Prompt 怎么写
```

Context Engineering 更关心：

```text
这一次调用 LLM，到底应该给它看什么？
```

Context 不只是 Prompt，还可能包括：

```text
System Prompt
Conversation
Memory
Retrieved Documents
Tool Definitions
Tool Results
Plan
Current State
Files
```

随着 Agent 执行时间变长，这些信息会越来越多，所以 Context 必须主动管理。

---

## 19. Context Selection

掌握程度：实践

不要：

```text
有的信息全部塞进去
```

而应该：

```text
从所有信息
 ↓
选当前任务真正需要的信息
 ↓
构造 Context
```

---

## 20. Context Compression / Compaction

掌握程度：核心实践 + Demo

例如：

原始历史：

```text
100 条 Conversation
50 个 Tool Result
20 个搜索结果
```

压缩为：

```text
任务目标：
xxx

已经完成：
1...
2...
3...

关键发现：
...

剩余问题：
...
```

然后：

```text
Summary
+
最近几条原始消息
+
当前任务相关资料
```

继续执行。

现代 Agent framework 已经开始直接提供自动 summarization / compaction 能力；但学习阶段最好亲手实现一次，否则很容易不知道框架到底替你做了什么。

---

## 21. Context Externalization

掌握程度：理解 + Demo

还有一种重要策略：

不要什么都放 Context。

而是写入：

```text
todo.md
plan.json
progress.md
database
filesystem
```

需要时再读取。

这对长时间运行的 Agent 尤其重要。

---

# 十、第九阶段：Harness

难度：⭐⭐⭐⭐

## 22. Agent Harness

掌握程度：核心概念 + 综合 Demo

Harness 不是某一种推理算法。

它更像：

> **负责把模型真正运行成 Agent 的外围运行系统。**

可能包括：

```text
Model Client
Agent Loop
Tool Registry
Tool Executor
Context Builder
Memory
State
Checkpoint
Retry
Permission
Sandbox
Logging
Tracing
Token Management
Compaction
Stop Condition
```

例如：

```text
        ┌──────────── Harness ─────────────┐

User → Context Builder → LLM
                         ↓
                     Tool Call
                         ↓
                   Tool Executor
                         ↓
                       State
                         ↓
                      Memory
                         ↓
                     Checkpoint

        └───────────────────────────────────┘
```

这也是理解 Claude Code、Codex 等 Coding Agent 的一个非常重要的概念。

长任务 Agent 的实践表明，仅仅做 Context Compression 仍然不够；还需要进度文件、Git 状态、feature list、checkpoint 等外部状态帮助新的 context window 恢复工作。

---

# 十一、第十阶段：自我检查与结果优化

难度：⭐⭐⭐⭐

## 23. Reflection

掌握程度：理解 + Demo

例如：

```text
生成答案
 ↓
检查答案
 ↓
发现问题
 ↓
修改答案
```

需要注意：

Reflection 并不是：

> “再问一次模型就一定更正确。”

关键在于有没有：

```text
Ground Truth
Validation Rule
Rubric
External Feedback
```

---

## 24. Evaluator-Optimizer

掌握程度：重点实践 + Demo

结构：

```text
Generator
 ↓
Result
 ↓
Evaluator
 ↓
Pass?
 ├─ Yes → Finish
 └─ No
      ↓
    Feedback
      ↓
    Generator
```

对于：

```text
代码生成
报告生成
SQL
结构化数据
```

尤其有价值。

这种 Generator → Evaluator → Feedback → Generator 的循环，也是常见 Agentic Workflow。

---

## 25. Deterministic Validation

掌握程度：重点实践

Agent 系统一个非常重要的工程思想：

> **能用代码判断的事情，不要全部交给 LLM 判断。**

例如：

```text
JSON是否合法 → JSON Schema
SQL能否执行 → Database
代码是否正确 → Test
页面是否正常 → Browser Test
文件是否存在 → File System
金额是否正确 → Program
```

LLM 更适合判断那些：

> 无法清晰编码成规则的问题。

---

# 十二、第十一阶段：Checkpoint 与长任务 Agent

难度：⭐⭐⭐⭐

## 26. Checkpoint

掌握程度：实践 + Demo

保存：

```text
当前 State
当前 Plan
完成步骤
Tool Results
```

Agent 崩溃之后：

```text
Load Checkpoint
 ↓
Resume
```

而不是：

```text
重新从第一步开始
```

---

## 27. Long-Running Agent

掌握程度：综合实践

需要综合：

```text
Plan
State
Memory
Checkpoint
Context Compression
External State
Retry
Evaluation
```

这是普通 ChatBot 和真正复杂 Agent 系统之间的重要区别。

---

# 十三、第十二阶段：Multi-Agent

难度：⭐⭐⭐⭐⭐

不要一开始学习 Multi-Agent。

如果连：

```text
Single Agent
Tool
State
Memory
Context
```

都没有真正理解，多 Agent 很容易变成：

> “让几个模型互相聊天。”

---

## 28. Multi-Agent

掌握程度：综合 Demo

例如：

```text
Research Agent
Data Agent
Analysis Agent
Writer Agent
```

分别承担不同任务。

---

## 29. Orchestrator-Worker

掌握程度：重点理解 + Demo

结构：

```text
             Orchestrator
                  ↓
       ┌──────────┼──────────┐
       ↓          ↓          ↓
   Worker A    Worker B    Worker C
       ↓          ↓          ↓
       └──────────┼──────────┘
                  ↓
               汇总结果
```

Anthropic 的 Research Agent 就使用了类似 Lead Agent + Subagents 的 orchestrator-worker 架构，让多个 Agent 分别拥有自己的 context 并并行探索。

---

## 30. Agent Handoff

掌握程度：理解 + Demo

Agent A：

```text
这个问题应该让数据库 Agent 处理。
```

于是：

```text
Agent A
 ↓
Handoff
 ↓
Agent B
```

重点理解：

```text
谁决定 Handoff？
传递什么 Context？
返回什么结果？
```

---

# 十四、第十三阶段：Skills、Tools、MCP

难度：⭐⭐⭐⭐

## 31. Tool

Tool：

> Agent 可以执行的一个能力。

例如：

```text
search()
queryDB()
sendEmail()
executeShell()
```

---

## 32. Skill

Skill 更接近：

> 一组可复用的工作知识 + 指令 + 工作流程。

例如：

```text
生成数据库设计文档 Skill
```

里面可能规定：

```text
先读取项目
再分析 Entity
再生成 ER
再输出 Markdown
```

Skill 内部仍然可能调用多个 Tool。

---

## 33. MCP

掌握程度：理解 + 实践

MCP 可以先简单理解成：

> **让 Agent 用统一方式发现和访问外部能力与上下文的一种协议。**

学习重点：

```text
MCP Client
MCP Server
Tools
Resources
Prompts
```

而不是一上来研究协议底层实现。

Agent 框架可以通过 MCP 把外部工具接入模型可使用的能力集合。

---

# 十五、第十四阶段：可靠性与安全

难度：⭐⭐⭐⭐⭐

## 34. Guardrail

包括：

```text
Input Guardrail
Output Guardrail
Tool Guardrail
```

例如：

```text
不允许删除生产数据库
```

必须在：

```text
Tool Executor
```

层进行权限限制。

而不是只写：

```text
System Prompt:
千万不要删除生产数据库。
```

---

## 35. Human-in-the-Loop

掌握程度：实践

例如：

```text
Agent:
我要删除这 20 个文件。

Harness:
需要用户确认。

User:
确认。

Harness:
执行 Tool。
```

高风险操作尤其需要这种机制。

---

## 36. Sandbox

掌握程度：理解

Coding Agent 尤其重要。

限制：

```text
Filesystem
Network
Shell
Database
Process
```

避免 Agent 拥有无限权限。

---

# 十六、第十五阶段：Observability 与 Evaluation

难度：⭐⭐⭐⭐⭐

## 37. Tracing

需要看到完整 Agent 执行过程：

```text
Prompt
 ↓
LLM
 ↓
Tool Call
 ↓
Tool Result
 ↓
LLM
 ↓
Tool Call
 ↓
...
```

否则 Agent 出错时非常难 Debug。

---

## 38. Metrics

例如：

```text
成功率
平均步骤
Token
Cost
Latency
Tool Error Rate
Retry Count
```

---

## 39. Agent Evaluation

传统代码：

```text
assertEquals(expected, actual)
```

Agent 很多时候输出不是完全确定的。

因此需要：

```text
Rule Based Eval
Test Case
LLM-as-Judge
Human Eval
Task Success Rate
```

---

# 十七、最终知识结构

整个学习路径可以压缩成：

```text
LLM
 │
 ▼
Prompt
 │
 ▼
Structured Output
 │
 ▼
Tool Calling
 │
 ▼
Agent Loop
 │
 ▼
ReAct
 │
 ▼
Workflow
 │
 ▼
Planning
 │
 ▼
State
 │
 ▼
Multi-turn Conversation
 │
 ▼
Memory
 │
 ▼
RAG
 │
 ▼
Context Engineering
 │
 ▼
Context Compression
 │
 ▼
Harness
 │
 ▼
Reflection / Evaluation
 │
 ▼
Checkpoint
 │
 ▼
Long-running Agent
 │
 ▼
Multi-Agent
 │
 ▼
MCP / Skill
 │
 ▼
Guardrail
 │
 ▼
Observability / Evaluation
```

---

# 十八、第一轮学习暂时不重点研究的内容

当前目标是掌握 Agent Engineering，因此以下内容暂时只知道存在即可：

```text
Transformer
Attention
Embedding 数学原理
Pre-training
Fine-tuning
SFT
RLHF
DPO
PPO
GRPO
Mixture of Experts
Quantization
Model Serving
CUDA
Distributed Training
```

以后如果要深入：

> “为什么模型能够推理？”

再进入这些模型层内容。

目前首先回答：

> **“怎么把一个 LLM 做成一个真正能够完成任务的 Agent？”**

这会更符合当前学习目标。