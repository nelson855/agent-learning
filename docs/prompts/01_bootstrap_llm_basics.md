# 实现 Prompt 01：LLM 无状态与多轮上下文 Demo

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 开始前必须先读取仓库根目录 `AGENTS.md`；不要创建额外的工具专属规则文件。

请先阅读仓库根目录 `AGENTS.md`、`docs/02_仓库架构与构建说明.md` 和教材 `docs/chapters/01_LLM到Agent_无状态与上下文.md`。

## 目标

本次只实现/修改模块：

```text
labs/lab01-llm-basics
```

技术栈严格使用：

- JDK 21
- Maven
- Jackson
- Java HttpClient
- JUnit 5

本章不要使用 SQLite。

## 实现

1. 定义：
   - `Message`
   - `Role`
   - `LlmClient`
   - `LlmResponse`
2. 实现 `OpenAiCompatibleLlmClient`
   - 从环境变量读取 `LLM_BASE_URL / LLM_API_KEY / LLM_MODEL`
   - 不硬编码 Key
3. 实现 `FakeLlmClient` 供测试使用。
4. 编写控制台聊天：
   - 用户输入追加到 `List<Message>`
   - 模型回答继续追加
   - `/reset` 清空 history
   - `/history` 打印当前消息数量和角色，不打印敏感 Key
5. README 清楚解释：
   - 这个 Lab 证明什么
   - 为什么这不是真正 Long-term Memory

## 验收

必须有单元测试验证：

- 连续两轮时第二次请求包含历史
- `/reset` 后 history 被清空
- 测试不依赖真实网络

不要实现 Tool Calling、Memory、数据库、Agent Loop。
