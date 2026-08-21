# Plan：lab07 Memory/知识日志 + 移除 demo

目标项目根：本目录（lab07-rag-context）。

请按以下步骤执行，只允许修改下方列出的两个文件，其余文件一律不得改动。

## 步骤 1 — 移除 demo 模式（Main.java）
- 删除 `main()` 中 `--demo` 分支（第 29 行），只保留 `runInteractive()`。
- 删除 `runDemo()` 方法（第 38-76 行）及其唯一引用。
- 确认交互模式是唯一真实入口。

## 步骤 2 — Memory 构建加日志（MemoryExtractor.java）
- 在 `extract()` 提取到记忆时，打印日志：
  `System.out.println("MEMORY EXTRACTED: [" + memoryType + "] " + content);`

## 步骤 3 — 交互式保存记忆加确认日志（Main.java runInteractive）
- 在保存记忆处（如 extract 后 save）打印：
  `MEMORY SAVED: [PREFERENCE] 用户偏好 ...`

## 步骤 4 — Knowledge 检索日志（确认已存在）
- `ContextBuilder.printRetrievalLogs()` 已打印 MEMORY RETRIEVAL / RAG RETRIEVAL，
  无需新增。确认交互模式下每轮问答都输出即可。

## 验证
- 在本目录运行 `mvn compile`，确保编译通过。

## 完成标准
- 四个步骤全部落地且编译通过后，报告每步改动摘要。