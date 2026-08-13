# 第 4 章：Workflow Patterns——不是所有事情都该交给 Agent 决定

## 4.1 Agent 不等于“全部自动决定”

很多系统一开始就犯一个错误：

> 既然用了 Agent，就让模型决定所有步骤。

但固定业务往往更适合 Workflow。

例如：

```text
生成摘要
→ 审核摘要
→ 保存数据库
```

这个顺序很稳定，没有必要让模型每次重新发明流程。

## 4.2 Prompt Chaining

```text
LLM A
→ Output
→ LLM B
→ Output
```

适合：

- 大任务拆成固定步骤
- 每一步输入输出明确

## 4.3 Routing

```text
Input
↓
Router
├─ 技术问题
├─ 任务操作
└─ 普通聊天
```

Router 可以：

- 程序规则
- LLM 分类
- 混合方式

## 4.4 Parallelization

多个互不依赖步骤可以并行：

```text
        ┌→ 查询任务统计
Input ──┼→ 查询最近失败
        └→ 查询新增任务
             ↓
            Merge
```

## 4.5 Workflow vs Agent

Workflow：

> 路径主要由代码定义。

Agent：

> 路径主要由模型结合当前状态动态决定。

现实系统经常是：

```text
Workflow
  ↓
某个节点调用 Agent
  ↓
返回 Workflow
```

而不是纯粹二选一。

## 4.6 本章 Demo

针对同一个需求做两个版本：

> 用户输入一个任务主题，生成任务标题和描述并保存。

### Version A：固定 Workflow

```text
generateTitle
→ generateDescription
→ validate
→ save
```

### Version B：Agent

提供 Tool，让模型自己决定先做什么。

然后比较：

- 哪个流程更稳定？
- 哪个更灵活？
- 哪个更容易测试？
- 哪个调用次数更可预测？

## 4.7 本章最重要的结论

> Agent 自主性不是越高越好。

应当把确定性留给程序，把不确定判断交给模型。

---

## 本章自测

1. Workflow 和 Agent 的关键差别是什么？
2. 为什么固定业务流程不一定适合 Agent？
3. Routing 是否一定要使用 LLM？
4. 一个系统能同时包含 Workflow 和 Agent 吗？

## 参考答案

1. 主要差别在下一步路径由程序预先决定，还是由模型根据环境动态决定。
2. Agent 会增加成本、随机性、测试难度和不可预测路径。
3. 不一定，确定规则优先用代码。
4. 可以，而且这是很常见、很合理的组合。
