# 实现 Prompt 10：Evaluator + Deterministic Validation

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；除 `AGENTS.md` / `CLAUDE.md` 双镜像外，不要创建额外的工具专属规则文件。

阅读：

- `docs/chapters/11_Reflection_Evaluator与确定性校验.md`
- `AGENTS.md`

本次只实现/修改模块：

```text
labs/lab10-evaluator
```

## 任务

根据 SQLite 任务统计生成 JSON 周报。

## Pipeline

```text
Generator
↓
Program Validator
↓
LLM Evaluator
↓
Pass?
└─ Feedback → Generator
```

最大迭代：

```text
3
```

## Program Validator

至少检查：

- JSON 可解析
- required fields
- 数字类型
- recommendations 非空

## LLM Evaluator

只判断语义：

- 是否解释异常
- 建议是否可执行

## 测试

Fake Generator 第一次生成结构错误：

程序应直接拒绝，不调用 Evaluator。

另一个用例：

结构正确但语义差，才进入 Evaluator。
