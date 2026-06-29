package top.gexingw.spring.transaction.message.rabbitmq;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * 单元测试：RabbitMQTransactionMessageSender 在不同投递模式下应调用 RabbitTemplate 对应的重载，
 * 并始终透传 CorrelationData（用于后续 Publisher Confirms 回调）。
 *
 * 注：当前实现统一走带 CorrelationData 的 convertAndSend 重载，这里使用 any(CorrelationData.class)
 * 校验"被传入"，对 id 的精确语义留待集成测试覆盖（参考后续关于 Publisher Confirms 设计意图的 issue）。
 */
class RabbitMQTransactionMessageSenderTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQTransactionMessageSender sender;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        sender = new RabbitMQTransactionMessageSender(rabbitTemplate);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void send_SimpleMode_CallsConvertAndSendWithQueue() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.SIMPLE, "test-queue", "", "");

        // 执行
        sender.send(message);

        // 验证：SIMPLE 模式按 queue 直接投递，payload + CorrelationData 一并传入。
        // 注意：RabbitTemplate 同时有 (String, Object, CorrelationData) 和 (String, String, Object) 两种 3 参重载，
        // mockito.eq("test payload") 返回 String 会造成 javac 编译期歧义。
        // 这里把 payload 显式声明为 Object，强制 javac 选中带 CorrelationData 的那个重载。
        Object payload = "test payload";
        verify(rabbitTemplate).convertAndSend(eq("test-queue"), eq(payload), any(CorrelationData.class));
    }

    @Test
    void send_FanoutMode_CallsConvertAndSendWithExchange() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.FANOUT, "", "test-exchange", "");

        // 执行
        sender.send(message);

        // 验证：FANOUT 模式按 exchange 广播，routingKey 为 ""（fanout 类型 exchange 会忽略）
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq(""), eq("test payload"), any(CorrelationData.class));
    }

    @Test
    void send_TopicMode_CallsConvertAndSendWithExchangeAndRoutingKey() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.TOPIC, "", "test-exchange", "test.routing.key");

        // 执行
        sender.send(message);

        // 验证：TOPIC 模式按 exchange + routingKey 投递
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test.routing.key"), eq("test payload"), any(CorrelationData.class));
    }

    @Test
    void send_TopicMode_PassesCorrelationDataWithMessageId() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.TOPIC, "", "test-exchange", "test.routing.key");

        // 执行
        sender.send(message);

        // 验证：CorrelationData.id 应携带消息 id 的字符串形式，供 Publisher Confirms 回调对账
        ArgumentCaptor<CorrelationData> captor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test.routing.key"), eq("test payload"), captor.capture());
        assertEquals("1", captor.getValue().getId());
    }

    private ITransactionMessage<Object> createTestMessage(MessageDeliveryMode deliveryMode, String queue, String exchange, String routingKey) {
        return new ITransactionMessage<Object>() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public Object getPayload() {
                return "test payload";
            }

            @Override
            public String getQueue() {
                return queue;
            }

            @Override
            public String getExchange() {
                return exchange;
            }

            @Override
            public String getRoutingKey() {
                return routingKey;
            }

            @Override
            public MessageDeliveryMode getDeliveryMode() {
                return deliveryMode;
            }

            @Override
            public @Nullable Integer getMaxRetryCount() {
                return ITransactionMessage.super.getMaxRetryCount();
            }
        };
    }
}
