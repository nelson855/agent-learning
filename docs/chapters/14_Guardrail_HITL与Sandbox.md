# 第 14 章：Guardrail、Human-in-the-loop 与 Sandbox

## 14.1 Prompt 不是权限系统

写：

```text
你绝对不能删除数据。
```

只是软约束。

真正的权限控制必须在 Tool Executor。

例如：

```java
if (toolCall.name().equals("deleteTask") && !permission.canDelete()) {
    return denied();
}
```

## 14.2 Guardrail 三层

### Input Guardrail

检查用户输入：

- 是否允许处理
- 是否包含非法参数
- 是否超过限制

### Tool Guardrail

最关键：

- 哪些 Tool 可调用
- 参数范围
- 资源范围
- 是否需要批准

### Output Guardrail

检查最终结果：

- 格式
- 敏感信息
- 必须字段
- 业务规则

## 14.3 Human-in-the-loop

高风险动作：

```text
删除
发送
付款
覆盖文件
执行生产 SQL
```

常见流程：

```text
Agent Proposed Action
↓
PENDING_APPROVAL
↓
Human Approve / Reject
↓
Execute / Cancel
```

这要求 State 支持：

```text
WAITING_APPROVAL
```

## 14.4 Sandbox

Coding Agent 尤其需要。

Sandbox 的核心：

> 即使 Agent 做出了错误决策，环境本身也限制它能造成的影响。

可以限制：

```text
filesystem root
network
process
SQL
commands
```

本教材不要求你真正实现 OS Sandbox，但必须理解边界。

## 14.5 本章 Demo

增加 Tool：

```text
deleteTask(taskId)
```

规则：

```text
删除 OPEN 任务必须人工确认
删除 DONE 任务直接拒绝
```

Agent 发起 Tool Call 后，不直接执行。

写入 SQLite：

```text
approval_request
```

CLI：

```text
/approve <id>
/reject <id>
```

批准后恢复 Run。

这个 Demo 会把前面的：

```text
State
Checkpoint
Resume
```

全部串起来。

---

## 本章自测

1. 为什么 System Prompt 不能替代 Tool Permission？
2. HITL 为什么属于 Agent State 的一部分？
3. Sandbox 和 Guardrail 有什么差别？
4. 哪类动作最适合人工确认？

## 参考答案

1. Prompt 是模型行为约束，不是系统执行层强制权限。
2. Agent 需要暂停、等待外部输入，然后从同一运行状态恢复。
3. Guardrail 是策略和校验；Sandbox 是环境能力隔离，即使策略失败也限制影响。
4. 高风险、不可逆、涉及外部副作用的动作。
