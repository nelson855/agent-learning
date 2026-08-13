# 实现 Prompt 04：Workflow vs Agent 对照实验

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `AGENTS.md`
- `docs/chapters/04_WorkflowPatterns.md`

本次只实现/修改模块：

```text
labs/lab04-workflow-vs-agent
```

实现同一个业务需求的两个版本：

> 根据用户主题生成任务标题、描述，验证后保存到 SQLite。

## Version A：Workflow

固定：

```text
generateTitle
→ generateDescription
→ deterministic validate
→ save
```

## Version B：Agent

提供 Tools，让 Agent 决定调用顺序。

## 输出比较

程序最后打印：

```text
model_call_count
tool_call_count
step_count
success
```

README 回答：

- 哪个更稳定？
- 哪个更灵活？
- 哪个更好测试？
- 哪个成本更可预测？

不要为了“赢”而故意把某一版写差。
