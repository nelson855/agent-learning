# lab06-state-memory

多轮对话、Agent State 与长期记忆（Long-term Memory）。

对应教材：`docs/chapters/06_多轮对话与AgentState.md`、`docs/chapters/07_Memory_短期长期与检索.md`
对应 Prompt：`docs/prompts/06_conversation_state_memory.md`

## 1. 本模块学什么

前面几个 Lab 的 Agent 都是"一次运行、内存即忘"。本 Lab 引入**持久化**，并把三个
容易混为一谈的概念分到三张不同的 SQLite 表：

| 概念 | 表 | 回答的问题 |
| --- | --- | --- |
| Conversation History（对话历史） | `message` | 这个会话聊过什么（流水账，每条消息都存） |
| Agent State（Agent 状态） | `agent_run` | 这个任务干到哪了（goal / status / step 的状态机） |
| Long-term Memory（长期记忆） | `memory` | 用户跨会话值得记住的偏好/事实/约定（经过选择，不是每条都存） |

一句话：**"聊过什么"、"干到哪了"、"记住了什么"是三个不同的持久化维度**，
不是同一张表换名字。

## 2. 为什么需要这些概念

- 没有 **Conversation History**：模型每轮都失忆，多轮对话无法进行。
- 没有 **Agent State**：长任务中途崩溃后无法知道它进行到哪一步、要不要续跑。
- 没有 **Long-term Memory**：用户上次说"以后用 Maven"，下次换个会话就忘了——每次都要重新交代。

## 3. 如何运行

```bash
# 1) 跑全部测试（10 个，不依赖真实模型）
mvn test

# 2) 离线演示：Session A 保存偏好 → "退出程序" → Session B 检索偏好
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab06.Main -Dexec.args="--demo"

# 3) 交互模式（配置了真实模型才能智能回复）
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab06.Main
```

交互模式命令：

```text
/chat new          创建新会话并切换过去
/chat <id>         切换到已有会话（重启后也能恢复历史）
/state <runId>     查看一次 Agent 运行的状态（goal / status / step）
/memory            查看已保存的长期记忆
/exit              退出
```

## 4. 运行时应该观察什么

离线演示 `--demo` 会依次输出：

```text
---- Session A ----
>>> 用户: 以后我的 Java Demo 都使用 Maven。
MEMORY SAVED: [PREFERENCE] 用户的 Java Demo 都使用 Maven
FINAL: 好的，已记住：以后你的 Java Demo 都使用 Maven。

==== 进程退出（数据库已关闭，内存与对话历史全部丢弃） ====

---- Session B ----
>>> 用户: 帮我初始化一个 Java Demo。
MEMORY SKIPPED: 本条不需要长期保存
RETRIEVED MEMORY
  - [PREFERENCE] 用户的 Java Demo 都使用 Maven
FINAL: 好的，我会用 Maven 来初始化你的 Java Demo。
```

重点观察：

1. **`MEMORY SAVED`** —— 用户消息先被 Memory Extractor 判断，命中"值得保存"才落库；
   普通请求（Session B）则 `MEMORY SKIPPED`。
2. **`RETRIEVED MEMORY`** —— Session B 是"重启"后的新会话，没有任何对话历史，
   却靠 `memory` 表检索回了 Session A 的偏好。这就是长期记忆与对话历史的区别。
3. **`RUN r-xxxx` / STEP / MODEL_ACTION / FINAL** —— 每次运行同时写 `agent_run`，
   可用 `/state r-xxxx` 查看持久化的状态机（RUNNING → WAITING_TOOL → … → COMPLETED）。

## 5. 本模块刻意没有实现什么

- **向量检索（Vector DB / 语义相似度）**：memory 检索只用 keyword LIKE + type 过滤 +
  importance / recency 排序，够教学、够确定，不引入向量库。
- **记忆合并 / 去重 / 遗忘策略**：只增不删，importance 是写死的固定值。
- **Checkpoint / 断点续跑**：`agent_run` 只记录状态供查看，不会真正从 WAITING_TOOL 恢复执行。
- **记忆写入时的二次确认（HITL）**：Extractor 说存就存。
- **RAG（外部知识库）**：memory 存的是"关于用户"，不是"世界知识"。
- **Web UI**：本 Lab 是 CLI（按 Lab 约定不建网页）。
