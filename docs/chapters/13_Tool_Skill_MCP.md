# 第 13 章：Tool、Skill 与 MCP

## 13.1 Tool

Tool 是一个可执行能力。

例如：

```text
getTask
createTask
readFile
queryDatabase
```

Tool 最终需要落到真实执行器。

## 13.2 Skill

Skill 更像：

> 可复用的工作方法。

例如：

```text
“生成任务复盘报告”
```

可能描述：

```text
1. 查询任务统计
2. 查询失败任务
3. 归纳失败原因
4. 按模板生成报告
```

它可能组合多个 Tool。

所以：

```text
Tool = 能做什么
Skill = 应该怎么做某类工作
```

## 13.3 MCP

当 Tool 越来越多，如果每个 Agent 框架都自己定义：

```text
工具发现
参数协议
资源读取
调用方式
```

集成会很碎。

MCP 可以先从工程角度理解为：

> 一个让模型应用以标准方式连接外部工具和资源的协议层。

学习重点不是协议所有字段，而是：

```text
Client
Server
Tool Discovery
Tool Invocation
Resources
```

## 13.4 本章 Demo 分两步

### Demo A：本地 Skill

创建：

```text
skills/task-review/SKILL.md
```

包含：

- 目标
- 前置条件
- 可用 Tools
- 执行步骤
- 输出格式

Agent 在执行“任务复盘”前读取 Skill。

观察：

> Skill 本身不是 Tool，它是工作说明。

### Demo B：极简 MCP 概念演示

不急着实现完整协议。

先把原来的：

```text
ToolRegistry
```

改造成：

```text
ToolProvider
```

支持：

```text
listTools()
callTool()
```

然后做：

```text
LocalToolProvider
RemoteLikeToolProvider
```

理解“工具提供方与 Agent 解耦”。

等概念掌握后，再使用标准 MCP SDK/Server。

## 13.5 为什么不直接上完整 MCP

因为如果一开始只看到：

```text
MCP Server Annotation
MCP Client Config
```

你可能知道“怎么接”，却不知道 MCP 到底解决了什么边界问题。

---

## 本章自测

1. Tool 和 Skill 的本质差别是什么？
2. Skill 是否一定可执行？
3. MCP 主要解决哪一类工程集成问题？
4. 为什么建议先写 ToolProvider 抽象，再上完整 MCP？

## 参考答案

1. Tool 是原子可执行能力，Skill 是一套完成某类工作的可复用知识/流程。
2. 不一定；Skill 通常指导 Agent 组合 Tool 或步骤完成任务。
3. 外部工具、资源与模型应用之间的标准化发现和调用。
4. 先理解协议解决的抽象边界，避免只学会框架配置。
