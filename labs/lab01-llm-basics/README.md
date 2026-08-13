# lab01-llm-basics — LLM 无状态与多轮上下文

- 对应教材：`docs/chapters/01_LLM到Agent_无状态与上下文.md`
- 对应 Prompt：`docs/prompts/01_bootstrap_llm_basics.md`

## 这个 Lab 证明什么

1. 底层 Chat API 每次调用都是**无状态**的：`response = LLM(current_context)`，模型没有神秘的后台记忆；
2. 多轮对话的"连续性"不是模型内部自动保存，而是**应用层**在每次请求里重新提交完整消息历史；
3. 一旦清空 history（`/reset`），模型立刻"失忆"。

## 为什么这不是 Long-term Memory

- 这里的 history 只是进程内的 `List<Message>`，程序退出即消失，没有任何持久化；
- 真正的 Long-term Memory 需要持久化存储（本仓库后续会用 SQLite），并解决"什么该记、何时检索、如何写入"等问题；
- 把"会话历史"当成"长期记忆"是第一章最容易混淆的误区，所以本章**刻意不用数据库**。

## 如何运行

### 离线观察（默认）

未配置环境变量时自动使用 `FakeLlmClient`，不访问网络：

```bash
cd labs/lab01-llm-basics
mvn test
```

然后在 IDE 中运行 `Main.main()`，或命令行：

```bash
java -cp target/classes com.example.agentlearning.lab01.Main
```

### 接真实模型观察智能行为

在仓库根目录复制 `.env.example` 为 `.env`，填入真实配置后运行：

```bash
cp .env.example .env
# 编辑 .env，填入 LLM_API_KEY
```

取值优先级：**环境变量 > 根目录 `.env` 文件**（`EnvFile` 会从当前工作目录向上查找 `.env`）。

`.env` 已被 `.gitignore` 忽略，不会把 Key 提交到仓库。

## 运行时应该观察什么

1. 输入 `我有一只猫，叫豆包。`
2. 输入 `它叫什么？` —— 因为第二次请求携带了历史，模型能理解"它"指豆包；
3. 输入 `/reset` 清空历史；
4. 再输入 `它叫什么？` —— 失去上下文，模型无法知道"它"指谁；
5. 随时输入 `/history` 查看当前消息数量与角色列表（不打印内容与 Key）。

## 刻意没有实现

- Tool Calling / Function Calling
- SQLite 数据库持久化
- Agent Loop / ReAct
- Memory 检索 / RAG
- 流式输出

本 Lab 只观察一件事：**多轮连续性 = 应用重新构造上下文**。
