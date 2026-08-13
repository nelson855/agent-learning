# 实现 Prompt 00：初始化 Agent Learning Maven 多模块仓库

> 本 Prompt 与具体 AI 编程工具无关。适用于任何能够读取仓库文件、修改代码并执行 Maven/Java 命令的 AI 编程工具。
> 本 Prompt 只搭建仓库骨架，不实现任何 Agent 业务能力。

## 开始前必须阅读

请先读取：

```text
AGENTS.md
docs/00_教材总览.md
docs/01_学习路线与Demo总览.md
docs/02_仓库架构与构建说明.md
docs/03_技术基线与工程约定.md
docs/04_Web可视化调试台规范.md
```

仓库级统一约束是 `AGENTS.md` 与 `CLAUDE.md` 双镜像（内容一致，修改须同步）；不要创建 `CODEX.md` 等其他工具专属规则文件。

## 目标

把当前目录初始化为：

```text
一个 Git 仓库
+ 一个 Maven 聚合父工程
+ 14 个独立 Lab Maven Module
+ 4 个独立 Stage Maven Module
```

技术基线：

```text
JDK 21
Maven
SQLite JDBC
Jackson
JUnit 5
Java HttpClient
```

本步骤不要实现真实 LLM 调用、Tool Calling、Agent Loop、Memory、RAG、Planner 等 Agent 能力，也不要提前创建 Stage Web 页面。Web UI 在 Stage 2~4 的对应 Prompt 中按需加入。

## 一、创建根 Maven 聚合工程

创建根目录：

```text
pom.xml
```

要求：

1. `packaging` 为 `pom`；
2. `groupId` 可使用 `com.example.agentlearning`；
3. `artifactId` 使用 `agent-learning`；
4. Java release 固定为 21；
5. UTF-8；
6. 集中管理 Jackson、SQLite JDBC、JUnit 5 的稳定版本；
7. 集中管理 Maven Compiler / Surefire 等必要基础插件；
8. 不引入 Spring Boot、Spring AI、LangChain4j、ORM、Redis、MQ、Docker 相关依赖。

## 二、创建 Lab Modules

创建：

```text
labs/lab01-llm-basics
labs/lab02-tool-calling
labs/lab03-agent-loop-react
labs/lab04-workflow-vs-agent
labs/lab05-planning
labs/lab06-state-memory
labs/lab07-rag-context
labs/lab08-context-compaction
labs/lab09-checkpoint
labs/lab10-evaluator
labs/lab11-multi-agent
labs/lab12-skill-mcp
labs/lab13-guardrail-hitl
labs/lab14-observability-eval
```

每个 Module 至少创建：

```text
pom.xml
README.md
src/main/java/...
src/test/java/...
```

每个 Module 的 `pom.xml` 继承根父 POM。

为了让根目录第一次就能 `mvn test`，每个模块可以创建一个极小的占位 `Main` 和一个简单 smoke test，但：

- 不要提前实现本章 Agent 功能；
- 不要创建复杂包结构；
- 不要为了复用创建公共 Maven Module。

## 三、创建 Stage Modules

创建：

```text
stages/stage01-minimal-agent
stages/stage02-stateful-agent
stages/stage03-long-running-agent
stages/stage04-agent-harness
```

同样至少包含：

```text
pom.xml
README.md
src/main/java/...
src/test/java/...
```

Stage 不依赖任何 Lab Module。

## 四、根 pom.xml 注册全部 Module

根 `pom.xml` 的 `<modules>` 必须完整列出上述 18 个模块。

验证 Maven reactor 能识别全部模块。

## 五、创建 .gitignore

至少忽略：

```text
target/
.idea/
*.iml
.classpath
.project
.settings/
.env
*.db
*.db-shm
*.db-wal
```

不得忽略教材 `docs/`。

## 六、README

保留当前根 `README.md` 的教材入口信息，可以补充一个“仓库已初始化”小节，但不要删除教材使用说明。

每个模块 README 初始只需要说明：

- 模块名称；
- 当前是教学占位骨架；
- 对应教材/Prompt 路径；
- 以后由哪个 Prompt 实现。

## 七、不要做的事情

本步骤禁止：

```text
实现真实 Agent 逻辑
创建 agent-common / agent-core / agent-runtime 公共模块
引入 Spring Boot
引入 ORM
引入 Agent Framework
在初始化阶段创建 Web 接口
在初始化阶段创建前端（Stage 2~4 会在对应综合 Prompt 中再创建）
创建 Docker 配置
创建 AGENTS.md / CLAUDE.md 之外的工具专属规则文件
```

## 八、验收

必须实际执行并汇报：

```bash
java -version
mvn -version
mvn test
```

验收条件：

1. Java 为 21；
2. 根 Maven reactor 能找到全部 18 个模块；
3. `mvn test` 成功；
4. 没有 Agent 业务实现；
5. 除 AGENTS.md / CLAUDE.md 外没有额外工具专属规则文件；
6. 根目录结构与 `docs/02_仓库架构与构建说明.md` 一致。

最后输出：

- 创建的目录树；
- 根 POM 的 module 清单；
- 测试结果；
- 明确说明“尚未实现任何 Agent 能力”。
