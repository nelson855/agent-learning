# 实现 Prompt 08：Context Engineering + Compaction

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/09_ContextEngineering与Compaction.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab08-context-compaction
```

## 实现

明确类：

```text
ContextBuilder
ConversationSummarizer
ContextPolicy
```

默认策略：

```text
COMPACT_AFTER = 20 messages
RECENT_MESSAGES = 10
```

超过阈值后：

```text
旧消息 → Structured Summary → SQLite
```

Summary 至少包含：

```text
goal
completed
importantFacts
decisions
openQuestions
pendingActions
```

## Demo

提供命令或脚本快速生成 30~50 轮 Fake 对话。

打印：

```text
RAW_HISTORY_COUNT
SUMMARY_VERSION
RECENT_COUNT
FINAL_CONTEXT_COUNT
```

## 测试

验证：

- 未达阈值不压缩
- 达阈值会生成 summary
- 最近消息不会被误删
- summary 可在程序重启后读取
