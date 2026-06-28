# Changelog

本文档记录所有重要变更，格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
- 项目治理基础设施：LICENSE、CONTRIBUTING、CODE_OF_CONDUCT、SECURITY、CHANGELOG
- Issue / PR 模板，CI 工作流，dependabot 配置
- `ROADMAP.md` 长期规划与待办清单

### Changed
- _无_

### Fixed
- _无_

---

## [0.0.3.3] - 2025-08-17

### Added
- 初始版本：基于 Spring 的事务消息实现
- 核心模块 `spring-transaction-message`：本地消息表 + afterCommit 钩子 + 定时重试
- RabbitMQ 适配模块 `spring-transaction-message-rabbitmq`
- 示例工程 `spring-transaction-message-example-rabbitmq`

[Unreleased]: https://github.com/geXingW/transaction-message/compare/v0.0.3.3...HEAD
[0.0.3.3]: https://github.com/geXingW/transaction-message/releases/tag/v0.0.3.3
