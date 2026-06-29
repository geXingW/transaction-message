# Contributing Guide

感谢你对本项目的关注！本文档说明如何参与贡献，请认真阅读后再提交代码。

中文 | [English](CONTRIBUTING_EN.md) <!-- TODO: 英文版待补 -->

---

## 行为准则

参与本项目即视为接受 [行为准则](CODE_OF_CONDUCT.md)。

## 我可以做什么

- 🐛 报告 Bug：提 [Bug Report](.github/ISSUE_TEMPLATE/bug_report.yml)
- ✨ 提需求 / 改进建议：提 [Feature Request](.github/ISSUE_TEMPLATE/feature_request.yml)
- 📝 改文档：README / ROADMAP / Wiki，typo 也欢迎
- 💻 写代码：从 [`good first issue`](https://github.com/geXingW/transaction-message/labels/good%20first%20issue) 开始
- 💬 参与讨论：[GitHub Discussions](https://github.com/geXingW/transaction-message/discussions) <!-- TODO: 开启 -->

## 提交流程

### 1. 先开 Issue（重要）

**任何代码改动都先开 Issue 讨论**，避免做完了 PR 才发现方向不对。例外：

- 修 typo
- 文档措辞调整
- 显而易见的 Bug 一行修复

### 2. Fork & Clone

```bash
git clone https://github.com/<你的用户名>/transaction-message.git
cd transaction-message
git remote add upstream https://github.com/geXingW/transaction-message.git
```

### 3. 建分支

分支命名规范：
- `feature/<issue-id>-<short-desc>` — 新功能
- `fix/<issue-id>-<short-desc>` — Bug 修复
- `docs/<short-desc>` — 文档
- `chore/<short-desc>` — 工程化、CI、依赖

示例：`fix/1-send-method-logic`

### 4. 写代码 + 测试

- **必须有测试**（Bug 修复要有回归测试，新功能要有功能测试）
- 测试用 JUnit 5 + AssertJ + Testcontainers（集成测试）
- 单元测试覆盖率核心模块不低于 70%

### 5. 本地验证

```bash
mvn clean verify
```

确保：
- 编译通过
- 所有测试通过
- 没有引入新的 lint 警告

### 6. 提交

**Commit Message 遵循 [Conventional Commits](https://www.conventionalcommits.org/)**：

```
<type>(<scope>): <subject>

<body>

<footer>
```

`type` 取值：
- `feat` — 新功能
- `fix` — Bug 修复
- `docs` — 文档
- `test` — 测试
- `refactor` — 重构（无功能变更）
- `perf` — 性能优化
- `chore` — 工程化
- `ci` — CI 配置

示例：
```
fix(core): correct send() method logic for new vs existing messages

Resolves the issue where new messages were inserted but afterCommit
hook never registered, causing messages to be stored but never sent.

Closes #1
```

### 7. 提 PR

- PR 标题用 Conventional Commit 格式
- 描述里 **必须关联 Issue**：`Closes #N` / `Fixes #N`
- 填好 PR 模板的所有检查项
- CI 必须全绿才能合并
- 至少 1 个 reviewer approve

### 8. Review 后

- 收到 review 意见 24h 内回复
- `Resolve conversation` 由 reviewer 操作，不要自己点掉
- 修改后 force push 同分支即可（不要 merge master，用 rebase）

---

## 代码规范

### Java

- JDK 8 兼容（Phase 4 才会有 JDK 17 分支）
- 4 空格缩进，UTF-8
- 类 / 方法必须有 Javadoc（包括 `@author`、`@since`、`@param`、`@return`、`@throws`）
- 不引入新的 SonarLint Critical / Blocker 级别问题
- 不要 commit 注释掉的代码（用 git history 找回）

### 包结构

DDD 分层，固定四层：

```
domain.<aggregate>              — 领域模型 / 仓储接口 / 工厂
application.service             — 应用服务（业务用例）
infrastructure.config           — Spring 自动装配 / 配置属性
infrastructure.repository       — 仓储实现
infrastructure.support          — 内部 SPI 接口（如 TaskExecutor）
infrastructure.util             — 工具类
infrastructure.converter        — 转换器
adapter.<channel>               — 适配层（mq / web / schedule）
```

### 测试规范

- 文件命名：被测类 + `Test` 后缀（单元测试） / + `IT` 后缀（集成测试）
- 单测放 `src/test/java`，集成测试放 `src/test/java` 并用 `@Tag("integration")`
- Testcontainers 用最小镜像（如 `mysql:8.0`、`rabbitmq:3-management`）

---

## 版本与分支策略

- `master` — 受保护分支，只接受 PR 合并
- `release/x.y` — 发布分支（仅维护期长的版本需要）
- 版本号遵循 [Semantic Versioning 2.0](https://semver.org/lang/zh-CN/)
- 0.x 版本视为开发版，API 不保证兼容

## 发布流程

由 maintainer 执行：

1. 更新 `CHANGELOG.md`
2. 改 pom.xml 版本号
3. 打 tag：`git tag -a v0.1.0 -m "Release 0.1.0"`
4. push tag 触发 release workflow
5. 发布 Maven Central + GitHub Release

---

## 沟通渠道

- Issue / PR — 优先
- Discussions — 设计讨论、问答
- Email — 紧急安全问题（见 [SECURITY.md](SECURITY.md)）

## License

提交贡献即表示你同意你的代码以 [Apache License 2.0](LICENSE) 授权给本项目。
