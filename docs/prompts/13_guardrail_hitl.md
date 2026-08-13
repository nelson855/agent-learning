# 实现 Prompt 13：Guardrail + HITL

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/14_Guardrail_HITL与Sandbox.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab13-guardrail-hitl
```

新增 Tool：

```text
deleteTask(taskId)
```

规则：

```text
OPEN → 需要人工批准
DONE → 禁止删除
其它状态 → 根据明确规则处理
```

SQLite：

```text
approval_request
```

Agent Run 支持：

```text
WAITING_APPROVAL
```

CLI：

```text
/approve <approvalId>
/reject <approvalId>
```

批准后从 Checkpoint / State 恢复。

测试必须证明：

- 模型无法绕过 Tool Guardrail
- 未批准时 delete 不执行
- reject 后 Run 正确结束或回到 Agent
