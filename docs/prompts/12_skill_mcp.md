# 实现 Prompt 12：Skill + MCP 概念层

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/13_Tool_Skill_MCP.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab12-skill-mcp
```

## Part A：Skill

创建：

```text
skills/task-review/SKILL.md
```

Agent 执行“任务复盘”前加载 Skill。

Skill 包含：

- goal
- required tools
- steps
- output contract

## Part B：ToolProvider

定义：

```java
interface ToolProvider {
    List<ToolDefinition> listTools();
    ToolResult callTool(ToolCall call);
}
```

实现：

```text
LocalToolProvider
MockRemoteToolProvider
```

目的是理解：

> Agent 与工具提供方解耦。

## 约束

这一版不要求实现完整标准 MCP 网络协议。

README 必须明确：

- Tool 是什么
- Skill 是什么
- MCP 解决什么
