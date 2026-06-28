# transaction-message

> 基于数据库的可靠任务执行引擎（Reliable Task Engine）—— 通用化 Outbox 模式实现。

[![CI](https://github.com/geXingW/transaction-message/actions/workflows/ci.yml/badge.svg)](https://github.com/geXingW/transaction-message/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/top.gexingw/spring-transaction-message.svg)](https://central.sonatype.com/artifact/top.gexingw/spring-transaction-message)

⚠️ **本项目正处于早期开发阶段（0.x），API 可能在 minor 版本之间发生变化。**

---

## 它解决什么问题

业务场景里经常遇到「DB 操作 + 异步动作」必须同时成功的需求：

- 下单成功后发 MQ 通知库存
- 注册成功后调用第三方接口推送 CRM
- 支付完成后异步执行积分计算

**直接调 MQ / HTTP 不可靠**：DB commit 之后服务宕机 → 异步动作丢失；DB 回滚但 MQ 已发 → 状态不一致。

**本项目的方案**：把「待执行任务」和业务数据 **同一个事务** 写入 DB → 后台调度器扫描重试 → 至少一次执行。

---

## 核心能力

- ✅ 数据库事务保证「任务一定落库」
- ✅ 业务事务提交后自动触发执行（`afterCommit` 钩子）
- ✅ 失败自动重试，指数退避，可配置最大次数
- ✅ 支持多种执行器（规划中）：
  - **MQ** — RabbitMQ ✅ / Kafka 🚧 / RocketMQ 🚧
  - **HTTP** — REST 调用 / Webhook 🚧
  - **LOCAL** — 反射调用本地 Bean 方法 🚧
- ✅ 至少一次（at-least-once）投递保证
- 🚧 多实例并发抢锁、死信队列、管理端 UI

---

## Quick Start

> ⚠️ 0.1.0 发布前不要在生产环境使用。当前 master 分支处于重构中，参见 [ROADMAP.md](ROADMAP.md)。

### 1. 引入依赖

```xml
<dependency>
    <groupId>top.gexingw</groupId>
    <artifactId>spring-transaction-message-rabbitmq</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. 建表

```sql
-- 详细脚本见 docs/schema.sql
CREATE TABLE transaction_message ( ... );
```

### 3. 配置

```yaml
spring:
  transaction:
    message:
      max-retry-count: 3
      retry-interval: 3s
```

### 4. 使用

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final TransactionMessageService messageService;

    @Transactional
    public void createOrder(OrderDTO dto) {
        // 1. 业务操作
        orderRepository.save(order);

        // 2. 注册事务消息（同一个事务）
        messageService.send(new OrderCreatedMessage(order.getId()));
        // 事务提交后自动发送到 MQ，失败会重试
    }
}
```

---

## 文档

- 📖 [架构设计](docs/architecture.md) <!-- TODO -->
- 🗺️ [路线图与待办](ROADMAP.md)
- 📝 [变更日志](CHANGELOG.md)
- 🤝 [贡献指南](CONTRIBUTING.md)
- 🔐 [安全策略](SECURITY.md)

---

## 设计理念

| | 本项目 | RocketMQ 事务消息 | Seata | Temporal |
|---|---|---|---|---|
| 定位 | 本地 Outbox | MQ 原生支持 | 分布式事务 | 工作流编排 |
| 依赖 | DB + 任意 MQ/HTTP | RocketMQ | TC 协调器 | 独立集群 |
| 一致性 | 最终一致 | 最终一致 | 强一致 | 最终一致 |
| 复杂度 | 低 | 中 | 高 | 高 |
| 学习成本 | 低 | 中 | 高 | 高 |

**本项目的取舍**：用最低的复杂度（一张表 + 一个调度器）解决最常见的 80% 场景。

---

## 模块结构

```
transaction-message/
├── spring-transaction-message/          # 核心模块（domain / application / infrastructure）
├── spring-transaction-message-rabbitmq/ # RabbitMQ binder
└── spring-transaction-message-example/  # 示例工程
    └── spring-transaction-message-example-rabbitmq/
```

---

## License

[Apache License 2.0](LICENSE)
