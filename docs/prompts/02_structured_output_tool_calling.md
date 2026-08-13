# 实现 Prompt 02：Structured Output + 单次 Tool Calling

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

先阅读：

- `AGENTS.md`
- `docs/chapters/02_StructuredOutput与ToolCalling.md`

本次只实现/修改模块：

```text
labs/lab02-tool-calling
```

## 技术栈

- JDK 21
- Maven
- SQLite
- JDBC
- Jackson
- JUnit 5

依赖建议：

```text
org.xerial:sqlite-jdbc
com.fasterxml.jackson.core:jackson-databind
org.junit.jupiter:junit-jupiter
```

## 目标

实现三个 Tool：

```text
getTask(taskId)
createTask(title)
calculator(expression)
```

任务使用：

```text
./data/lab02.db
```

## 必须自己实现的概念

- `ToolDefinition`
- `ToolCall`
- `ToolResult`
- `ToolRegistry`
- 参数解析和验证
- Unknown Tool 错误

本章只允许：

```text
一次模型调用
→ 0 或 1 次 Tool
→ 程序展示结果
```

不要做 Agent Loop。

## 测试

至少覆盖：

1. 正确创建任务
2. 查询不存在任务
3. Tool 参数缺失
4. 不存在的 Tool
5. SQLite schema 自动初始化

提供 `FakeLlmClient`，让测试完全确定。
