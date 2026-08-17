# lab07-rag-context

Memory 与 RAG 的区别、Context 的组装（Context Builder）。

对应教材：`docs/chapters/08_RAG_Memory_Context区别.md`
对应 Prompt：`docs/prompts/07_rag_context.md`

## 1. 本模块学什么

三个最容易混用的词，在代码里各指什么：

| 概念 | 本模块对应物 | 回答的问题 |
| --- | --- | --- |
| **Context**（上下文） | `ContextBuilder` 组装出的这一次 LLM 调用的全部消息 | 这一次调用，模型实际能看到什么？ |
| **Memory**（记忆） | `memory` 表 + `MemoryRetriever` | 关于"用户"的长期偏好/约定，值得以后继续用 |
| **RAG / Knowledge**（知识） | `knowledge_doc` 表 + `KnowledgeRetriever` | 外部资料库里有什么与当前问题相关的知识 |

一次问答的完整路径：

```text
问题
 → MemoryRetriever.retrieve（查 memory 表）        MEMORY RETRIEVAL
 → KnowledgeRetriever.retrieve（查 knowledge_doc 表） RAG RETRIEVAL
 → ContextBuilder.build                              CONTEXT SUMMARY
     System Prompt
   + Retrieved Memory
   + Retrieved Knowledge
   + Recent Messages
   + Current Request
 → LLM 回答
```

## 2. 为什么 Memory 和 RAG 底层都能存 SQLite，语义却不同

两者都可能"存文本 → 检索 → 放进 Prompt"，技术上完全一样（本模块甚至都是 `LIKE '%词%'`）。
区别在**这条文本来自哪里、代表什么**：

```text
"项目使用 Maven 构建"
 ├─ 如果这是用户过去明确说的偏好    → 它是 Memory（存 memory 表，回答"我/我的"类问题）
 └─ 如果这是项目技术规范文档中的规则 → 它是 Knowledge（存 knowledge_doc 表，回答"本项目"类问题）
```

本模块 `MemoryVsRagTest` 用同一个事实证明了这一点：
`MemoryRepository` 只查 `memory` 表，`KnowledgeRetriever` 只查 `knowledge_doc` 表，
两边都能命中同一条"Maven 构建"信息，但来源、用途、类型各自独立。

> 不要问"这个内容存 Redis 还是向量库"，先问"它从语义上到底属于什么"（教材 8.6）。

## 3. 如何运行

```bash
# 1) 跑全部测试（13 个，不依赖真实模型）
mvn test

# 2) 离线演示：导入 3 篇文档 + 保存偏好 + 三个对照问题
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab07.Main -Dexec.args="--demo"

# 3) 交互模式（配置了真实模型才能智能回答）
mvn compile exec:java -Dexec.mainClass=com.example.agentlearning.lab07.Main
```

知识文档在 `src/main/resources/knowledge/`，启动时自动导入 `knowledge_doc` 表（幂等：先清空再导入）。

## 4. 运行时应该观察什么

`--demo` 的三个对照问题：

1. **「任务系统使用什么数据库？」** —— `MEMORY RETRIEVAL (无命中)`，`RAG RETRIEVAL` 命中 3 篇知识文档，回答来自项目规范（SQLite）。
2. **「我的项目用什么构建？」** —— `MEMORY RETRIEVAL` 命中用户偏好（Maven），回答来自 Memory。
3. **「项目使用什么构建工具？」** —— Memory 与 RAG **同时命中**同一条"Maven"事实，`CONTEXT SUMMARY` 显示 `memory: 1 条 / knowledge: 3 条`——这正是教材 8.4 的"同一条事实，两种身份"。

每次问答看三行日志：`MEMORY RETRIEVAL`、`RAG RETRIEVAL`、`CONTEXT SUMMARY`，
分别对应 Memory 检索、RAG 检索、Context 组装。

## 5. 本模块刻意没有实现什么

- **Embedding / 向量数据库**：检索只用 keyword（title/content LIKE）+ tags，中文按二元组近似切分。
- **真实中文分词 / 语义排序**：所以"使用、工具"这类通用词会拉宽命中范围；向量库能做得更好，但那是后话。
- **多轮对话**：Context 的 Recent Messages 部分被组装，但 demo 是单轮问答，recent 恒为空。
- **工具调用**：本章只讲 Context 组装，Agent 循环回归 lab03/stage01。
- **Memory 写入流程**：Extractor 保留（保存偏好用），重点放在检索侧。
- **Web UI**：本 Lab 是 CLI。
