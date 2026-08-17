# lab08-context-compaction

长对话的 Context 压缩（Compaction）：结构化摘要 + 最近消息保留。

对应教材：`docs/chapters/09_ContextEngineering与Compaction.md`
对应 Prompt：`docs/prompts/08_context_compaction.md`

## 1. 本模块学什么

模型上下文窗口有限，长对话不能一直原样堆下去。本模块实现"真正的压缩"：

```text
长对话
 → 超过阈值（默认 20 条）时触发压缩
 → 旧消息 → LLM 生成结构化摘要（goal / completed / importantFacts / decisions / openQuestions / pendingActions）
 → 摘要存 conversation_summary 表（带递增 version）
 → 旧消息从 message 表删除，只保留最近 10 条
 → 下一次调用组装：System + [Summary vN] + 最近 10 条 + Request
```

关键观察点是"压缩"和"简单删除"的区别——**信息不丢，只是被浓缩**：

| 对比 | 简单丢弃历史 | 本模块的 Compaction |
| --- | --- | --- |
| 存储 | 消息删了就是没了 | 旧消息删除，但结构化摘要落库可读回 |
| 上下文 | 后续调用看不见过去 | Summary 块注入，未完成事项/决定/事实仍可见 |
| 成本 | 省了 token，丢了记忆 | 省了 token，保住可恢复信息 |
| 重启 | 无状态 | Summary 跨重启持久化 |

核心文件：

| 文件 | 职责 |
| --- | --- |
| `ContextPolicy` | 策略：COMPACT_AFTER（多少条触发）、RECENT_MESSAGES（保留最近几条） |
| `ConversationSummarizer` | 把旧消息发给 LLM，强制输出结构化 JSON 摘要 |
| `ConversationSummaryParser` | 程序校验模型输出，字段缺失用空数组兜底 |
| `CompactionService` | 追加消息 → 超阈值自动压缩 → 记录 RAW_HISTORY_COUNT 总产量 |
| `ContextBuilder` | 组装 System + Summary + Recent + Request，打印 SUMMARY_VERSION / RECENT_COUNT / FINAL_CONTEXT_COUNT |
| `MessageRepository` | message 表存取 + `deleteOldestMessages`（真删旧消息） |
| `ConversationSummaryRepository` | conversation_summary 表存取，`nextVersion` 递增版本 |

## 2. 为什么不是"把整个历史都塞进去"

Context 是缓存，不是数据库（教材 9.3）。判断一条信息该放哪：

```text
这一轮任务推进需要吗？
 ├─ 需要，但最近才说过 → 放 Recent Messages（最近 10 条）
 ├─ 需要，但发生在很久以前 → 放 Summary（结构化浓缩）
 └─ 以后可能用，但不是本轮任务 → 放 Memory / 知识库（lab06 / lab07），根本不该进上下文
```

Compaction 让"很久以前的对话"从原始消息变成结构化摘要，
即保留了**这一轮需要的那部分**（决定、未完成事项、关键事实），又释放了上下文空间。

## 3. 如何运行

```bash
# 1) 跑全部测试（8 个，不依赖真实模型）
mvn test

# 2) 离线演示：生成 38 轮假对话（76 条消息），自动压缩 6 次
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab08.Main -Dexec.args="--demo"

# 3) 交互模式（配置了真实模型则走真实 LLM，否则脚本兜底）
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab08.Main
```

交互模式命令：直接输入继续对话；`/summary` 查看最新摘要；`/exit` 退出。

## 4. 运行时应该观察什么

`--demo` 会依次打印：

1. 每次压缩一行 `[COMPACT] N 条旧消息 → Summary vX，保留最近 10 条`——旧消息被真删，摘要落库。
2. 压缩统计：
   - `RAW_HISTORY_COUNT`（76）：本轮**累计产出**的原始消息总数——即使消息被删除，总数仍累计，能看清"产出了多少"；
   - `SUMMARY_VERSION`（6）：最新摘要版本——压缩过几次；
   - `RECENT_COUNT`（10）：message 表**实际保留**的条数——对比总产量，看出上下文被压缩了 7 倍以上；
   - `FINAL_CONTEXT_COUNT`（13）：一次 buildContext 组装的消息数 = System + Summary + 10 条最近 + 请求。
3. 最新摘要内容：`[CONVERSATION SUMMARY v6]` 的 goal / completed / importantFacts / decisions / openQuestions / pendingActions——被压缩的信息仍可读。

再配合测试看"重启后可恢复"：`CompactionServiceTest.summaryPersistsAcrossReopen`
关闭数据库再重开，摘要版本与保留消息仍能读回。

## 5. 本模块刻意没有实现什么

- **摘要的加权/分层**：这里是"超过阈值就把最旧的一批全部压缩"，没有按消息重要性挑选压缩对象。
- **多级摘要（Summary of Summaries）**：超长会话压缩 N 次后，只把最新一份摘要注入上下文；更早的摘要仍在表里但不再注入。
- **记忆/知识库**：压缩只处理对话本身，用户偏好、项目知识仍属于 lab06 / lab07 的存储，不混进本模块。
- **窗口管理（滑动窗口截断）**：那是"丢"不是"压"；本模块演示的是保留信息可恢复的压缩。
- **向量检索的摘要召回**：读取哪份摘要目前用"版本号最新"，没有做语义匹配。
- **Web UI**：本 Lab 是 CLI。
