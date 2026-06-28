# ROADMAP & TODO

> 项目长期规划 + 待办清单。每完成一项勾掉，新发现的问题/想法追加到对应阶段。
> 维护人：@geXingW　最近更新：2025-08-17 之后的迭代

---

## 0. 项目定位

**一句话**：基于 DB 的可靠任务执行引擎（通用化 Outbox 模式）。

**核心承诺**：
1. 业务事务把"待执行任务"写入数据库
2. 利用数据库事务保证任务存储原子性，业务成功 = 任务必然落库
3. 后台调度器定时扫描，把任务投递给执行器
4. **保证至少一次（at-least-once）** 执行；消费侧通过 `biz_key` 幂等去重

**支持的执行器（Executor）**：
- `MQ`     → RabbitMQ / Kafka / RocketMQ / Pulsar
- `HTTP`   → REST 调用 / Webhook（支持签名、重定向、超时）
- `LOCAL`  → 反射调用本地 Spring Bean 方法（适合异步任务、补偿）

**非目标**：
- 不做分布式事务（不是 Seata / DTM）
- 不做工作流编排（不是 Temporal / Activiti）
- 不做强一致性（at-least-once，不是 exactly-once）

---

## 1. 当前状态

- 版本：`0.0.3.3`
- 核心思路对（Outbox + afterCommit + 定时重试），但**实现层 bug 较多，跑不通 happy path**
- 抽象层级偏低，`ITransactionMessage` 接口和 RabbitMQ 概念耦合，无法表达 HTTP / 本地任务
- 仅有 RabbitMQ 一种 Sender，example 也只能跑 MQ
- 无测试、无 schema.sql、无文档

---

## 2. Phase 1 — 修稳现版本（目标版本 0.1.0，预计 1-2 周）

**目标**：不改架构，先让 RabbitMQ 流程能完整跑通。

### 2.1 致命 Bug 修复（P0）

- [ ] **Bug-1** `JdbcTransactionMessageServiceImpl.send()` 主流程逻辑反了
  - 新消息插库后 `return`，从未触发 afterCommit → MQ 永不发送
  - 已存在的消息一进来就 `sendFail`，重试场景被误判
  - 修正：新消息 insert + 注册 afterCommit；重试场景直接 send 不应 sendFail
- [ ] **Bug-2** `SpringTransactionMessageRabbitMQAutoConfiguration` 自动装配失效
  - `@EnableAutoConfiguration` 用错（应为 `@AutoConfiguration` / `@Configuration`）
  - `transactionMessageSender` 方法缺 `@Bean` 注解
  - 缺 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
  - 修正后 example 不再需要手动 `@Bean TransactionMessageSender`
- [ ] **Bug-3** `nextRetryTime` 单位混乱
  - Factory 用毫秒、`sendFail` 用秒、查询传秒
  - 修正：全链路统一毫秒（推荐）或秒（二选一），写注释固定
- [ ] **Bug-4** payload 重复序列化
  - Factory 已 `JacksonUtil.toJson(payload)` 转字符串
  - Sender 又把字符串交给 `convertAndSend`，消费者收到带引号字符串
  - 修正：DB 存 JSON 字符串，发 MQ 前反序列化回对象（或保留原对象，仅 DB 落库时序列化）
- [ ] **Bug-5** `RabbitMQTransactionMessageSender` 没实现 `DIRECT` 模式
  - if 链漏了 DIRECT 分支，消息直接丢
  - 修正：补 DIRECT；改 switch-case；defult 抛异常告警
- [ ] **Bug-6** `sendSucceed()` 状态机漏洞
  - `setSendStatus(SUCCEED)` 后没 update，纯无效操作
  - `find()` 返回 null 时 `remove()` 会 NPE
  - 修正：先空判，直接 remove 即可（成功的消息不留底是合理的，但要确认这是约定）
- [ ] **Bug-7** ID 用 `System.currentTimeMillis()` 高并发会撞主键
  - 修正：雪花算法 / UUID / DB 自增三选一
  - 推荐雪花（保留时间排序 + 全局唯一），可复用作者已有的 `snowflake` 仓库

### 2.2 基础设施补全（P0）

- [ ] **schema.sql** 提供完整建表脚本
  - 字段：id / exchange / routing_key / queue / delivery_mode / payload / max_retry_count / retried_count / next_retry_time / send_status / created_at / updated_at
  - 索引：`(send_status, next_retry_time)` 覆盖扫描查询
  - 同步放到 `src/main/resources/schema.sql` + README 标注位置
- [ ] **README** 重写
  - 一段话定位、架构图、Quick Start（建库 → 引依赖 → 配 yml → 实现 ITransactionMessage → 调 send）
  - 重试机制说明、状态机说明、配置项表格
- [ ] **`spring.factories` → `AutoConfiguration.imports`** 迁移
  - Spring Boot 2.7+ 已经推荐新格式，旧文件保留兼容
- [ ] **example 清理**
  - 删 `RabbitMqConfiguration` 里手动 `@Bean TransactionMessageSender`（starter 修好后这段是多余的）
  - 删被注释掉的 `ConfirmCallback` 内部类
  - example 改名更清晰：`order-create-example`

### 2.3 测试补全（P0）

- [ ] **Testcontainers** 集成测试
  - 起 MySQL + RabbitMQ 容器
  - 测试：happy path / DB 提交失败 → 消息不发 / MQ ack=false → 重试 / 达到 max → FAILED
- [ ] **单元测试**
  - `TransactionMessage.sendFail()` 重试时间计算
  - `MessageConverter.toTransactionMessage()` SQL ResultSet 映射
  - `MessageDeliveryMode.of()` 边界值（null / 空 / 大小写）
- [ ] **JaCoCo 覆盖率报告**，目标核心模块 ≥ 70%

### 2.4 验收标准

- example 工程 `mvn spring-boot:run` 后调 `GET /web/order?id=1`
- 数据库出现一条 NORMAL 消息 → 立即被发送 → ack 后被删除
- 故意停掉 RabbitMQ，消息留在表里 → 重启后定时任务捞起来重发成功

---

## 3. Phase 2 — 抽象升级到 Task 模型（目标版本 0.2.0，预计 2-3 周）

**目标**：把"消息"抽象成"任务"，为支持 HTTP / 本地任务铺路。**这是最重要的一步，接口稳定后很难再大改**。

### 3.1 核心抽象重构

- [ ] **`ReliableTask`** 替代 `TransactionMessage`
  - 通用字段：id / biz_key / executor_type / executor_config(JSON) / payload / status / retry / lock
  - 不再有 exchange / routing_key / queue 等 MQ 字段（下沉到 executor_config）
- [ ] **`TaskExecutor`** 接口
  ```java
  interface TaskExecutor {
      String type();                          // MQ / HTTP / LOCAL
      ExecuteResult execute(ReliableTask t);  // 同步返回结果（成功/失败/重试）
  }
  ```
- [ ] **`TaskExecutorRegistry`** 按 type 路由到对应 executor
- [ ] 现有 MQ 逻辑迁移为 `MqTaskExecutor` 实现，保留向后兼容（旧 `ITransactionMessage` 接口转 adapter）

### 3.2 状态机升级

- [ ] 新增 `RUNNING` 中间态：`PENDING → RUNNING → SUCCESS / FAILED → DEAD`
- [ ] `RUNNING` 由调度器写入，并发抢锁场景下避免重复执行
- [ ] `DEAD` 表示达到最大重试次数，进死信，需人工干预

### 3.3 多实例并发

- [ ] 表加 `lock_owner` (instance_id) + `lock_until` (timestamp)
- [ ] 调度器查询用 `SELECT ... FOR UPDATE SKIP LOCKED` (MySQL 8+)
- [ ] 兼容方案：Redis 分布式锁（针对 MySQL 5.7 / 不支持 SKIP LOCKED 的库）
- [ ] 锁超时自动释放（防止节点挂掉锁不释放）

### 3.4 调度器下沉

- [ ] 调度器从 example 移到 core，作为 starter 默认能力
- [ ] `@ConditionalOnProperty(prefix="reliable-task", name="scheduler.enabled", havingValue="true", matchIfMissing=true)`
- [ ] 调度间隔、批量大小、最大并发可配置

### 3.5 ID 策略统一

- [ ] 引入 `IdGenerator` 接口，默认实现雪花
- [ ] 用户可注入自定义实现（DB 自增、UUID、Leaf 等）

---

## 4. Phase 3 — Executor 扩展（目标版本 0.3.0，预计 2-3 周）

**目标**：覆盖 MQ / HTTP / LOCAL 三种执行方式，完成核心承诺。

### 4.1 HTTP Executor

- [ ] `HttpTaskExecutor` 实现
  - 支持 GET / POST / PUT / DELETE
  - 自定义 Header、超时、重定向策略
  - 响应判定：HTTP 2xx → SUCCESS，4xx → FAILED 不重试，5xx / 超时 → 重试
  - 可选签名机制（HMAC / 自定义）
- [ ] 配置示例：webhook 通知、第三方回调

### 4.2 LOCAL Executor

- [ ] `LocalTaskExecutor` 实现
  - 通过 `beanName` + `methodName` + `args(JSON)` 反射调用
  - 注意类版本兼容（任务存了一周后 Bean 改了方法签名怎么办 → 失败告警）
  - args 序列化用 Jackson，必须可反序列化
- [ ] 提供 `@ReliableTaskMethod` 注解辅助声明可被调用的方法（白名单，避免任意反射）
- [ ] 适用场景：异步发邮件、补偿动作、延迟任务

### 4.3 消费侧幂等 Helper

- [ ] 提供 `idempotency_record` 表 + `@Idempotent(key="...")` 注解
- [ ] AOP 拦截：执行前查表，已处理直接返回；执行后写入记录
- [ ] 配套清理策略（保留 N 天后删除）

### 4.4 多 MQ Binder

- [ ] `reliable-task-kafka` 模块
- [ ] `reliable-task-rocketmq` 模块
- [ ] `reliable-task-pulsar` 模块（可选）
- [ ] 统一接口，用户切换 MQ 仅需换依赖 + 改配置

---

## 5. Phase 4 — 生态完善（持续迭代）

### 5.1 可观测性

- [ ] Micrometer 指标：积压数、成功率、TPS、P95 延迟、失败 Top N
- [ ] Spring Actuator 端点 `/actuator/reliable-task` 暴露统计
- [ ] 集成 Prometheus / Grafana 示例 dashboard

### 5.2 管理端

- [ ] REST 管理 API：查询 / 重投 / 终止 / 死信处理
- [ ] 简易 Web UI（Vue / React），看板展示积压、失败、TPS
- [ ] 支持手动重试单条 / 批量

### 5.3 文档与品牌

- [ ] VuePress / mkdocs-material 文档站
- [ ] 完整架构图、时序图（happy path / 重试 / 并发 / 死信）
- [ ] 选型对比文档（vs RocketMQ 事务消息 vs Seata vs DTM）
- [ ] 中英文 README

### 5.4 发布

- [ ] CI（GitHub Actions：build + test + publish snapshot）
- [ ] 发布 Maven Central（release profile 已配，缺 GPG 完整流程）
- [ ] 每个 minor 版本写 Release Notes + Migration Guide

### 5.5 跨版本支持

- [ ] Spring Boot 3.x 主分支（JDK 17+）
- [ ] Spring Boot 2.7 兼容分支（JDK 8）
- [ ] Spring Cloud 集成示例

---

## 6. 长期想法 / 未排期 backlog

- [ ] **延迟任务**：任务可指定执行时间（now + N），调度器扫描时按时间过滤
- [ ] **定时重复任务**：cron 表达式，每次执行完自动写下一次
- [ ] **任务依赖**：A 执行成功后才执行 B（轻量 DAG）
- [ ] **任务分组 / 标签**：批量操作、按业务线隔离
- [ ] **分库分表支持**：ShardingSphere 集成示例
- [ ] **MongoDB / PostgreSQL Repository** 实现
- [ ] **GraalVM Native Image** 兼容

---

## 7. 命名待定 / 重要决策点

- [ ] 项目改名？`transaction-message` 已不准确，候选：`reliable-task` / `spring-outbox` / `spring-reliable` / `dolphin` (TBD)
- [ ] groupId 是否变更（影响 Maven 坐标）
- [ ] 旧 `ITransactionMessage` 接口是否保留兼容层（保留 1-2 个 minor 后下线？）
- [ ] payload 序列化方式：JSON / 二进制 / 可插拔

---

## 8. 已完成 ✅

（按 commit 倒序追加）

- _暂无_
