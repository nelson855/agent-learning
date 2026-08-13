# lab02-tool-calling — Structured Output 与 Tool Calling

- 对应教材：`docs/chapters/02_StructuredOutput与ToolCalling.md`
- 对应 Prompt：`docs/prompts/02_structured_output_tool_calling.md`

## 这个 Lab 证明什么

1. **结构化输出**：模型把自然语言转成一个**字段明确的 JSON**（`{"tool":...,"arguments":{...}}`），程序才能可靠地读取、校验、执行 —— 而不是去猜一段自由文本的含义；
2. **模型是"参谋"，程序是"司令"**：模型只负责*建议*调用哪个工具、给什么参数；这个工具**存不存在、参数合不合法、能不能执行、怎么执行**，全部由程序决定（`ToolRegistry` 分派 + `ArgumentValidator` 校验）；
3. **Tool Call / Tool Result 是程序内部的对象**：一次用户输入，最多只有一次工具调用，把结果直接展示给用户，**不做多轮循环**（Agent Loop 是下一章的内容）。

## 本章自己实现的核心概念

| 概念 | 类 | 职责 |
| --- | --- | --- |
| 工具定义 | `ToolDefinition` | 告诉模型"这个工具有什么、怎么调用"（参数类型 + 必填） |
| 工具调用 | `ToolCall` | 模型建议的一次动作（工具名 + 参数），只是"建议" |
| 工具结果 | `ToolResult` | 执行结果：成功/失败 + 可展示文本，失败也是一种结果 |
| 工具注册表 | `ToolRegistry` | 注册、按名分派、`UNKNOWN_TOOL` 报错、生成给模型的说明 |
| 参数校验 | `ArgumentValidator` | 执行前拒绝缺失/类型错误的参数，而不是让它 NPE |
| 输出解析 | `ToolCallParser` | 把模型的 JSON 回复解析成"调用工具"或"纯文本回复" |
| 单步编排 | `ToolRunner` | 一次用户输入 → 模型 → 0/1 次工具 → 展示结果 |

## 三个教学工具

| 工具 | 参数 | 实现 |
| --- | --- | --- |
| `getTask` | `taskId: string` | 查 SQLite `task` 表；查不到返回"未找到" |
| `createTask` | `title: string` | 插入 `task` 表（id 由程序生成，status 初始 `pending`） |
| `calculator` | `expression: string` | 自实现四则运算求值器（`Calculator`），不引第三方库 |

任务数据存 SQLite：`./data/lab02.db`（测试用 `:memory:`）。schema 启动时自动初始化：

```sql
CREATE TABLE IF NOT EXISTS task (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

## 如何运行

### 离线观察（默认）

未配置环境变量时自动使用 `FakeLlmClient`，不访问网络：

```bash
cd labs/lab02-tool-calling
mvn test
```

然后在 IDE 中运行 `Main.main()`，或命令行：

```bash
java -cp target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) com.example.agentlearning.lab02.Main
```

### 接真实模型观察智能行为

在仓库根目录复制 `.env.example` 为 `.env`，填入 `LLM_API_KEY` 后运行：

```bash
cp .env.example .env
# 编辑 .env，填入 LLM_API_KEY
```

取值优先级：**环境变量 > 根目录 `.env` 文件**。`.env` 已被 `.gitignore` 忽略。

## 运行时应该观察什么

1. 输入 `帮我创建一个任务：写周报` —— 程序展示"已创建任务"与生成的 id；
2. 输入 `/tools` —— 查看给模型看的工具说明 JSON；
3. 输入 `计算 (1+2)*3` —— 程序展示 `= 9`；
4. 故意输入会让模型编造工具/给错参数的话，观察程序返回 `UNKNOWN_TOOL` 或"参数校验失败"而不是崩溃；
5. 用 sqlite 客户端查看 `data/lab02.db` 的 `task` 表，确认数据真的落了库。

## 刻意没有实现

- **Agent Loop / 多轮循环**：工具结果不会再次喂回模型（下一章 lab03 专门做）；
- Memory / RAG / Planner / Multi-Agent 等后续章节能力；
- OpenAI 原生 `tools`/`tool_calls` 协议参数：本章用"system prompt 约定 JSON 格式"的方式落地结构化输出，教学重点是程序侧的解析/校验/分派；
- 表达式求值仅支持四则运算，没有函数、变量、幂运算。

本 Lab 只观察一件事：**模型建议，程序把关**。
