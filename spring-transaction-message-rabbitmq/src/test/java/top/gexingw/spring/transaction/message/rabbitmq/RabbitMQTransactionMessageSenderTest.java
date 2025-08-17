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
import static org.mockito.Mockito.*;

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

        // 验证
        verify(rabbitTemplate).convertAndSend(eq("test-queue"), eq("test payload"));
    }

    @Test
    void send_FanoutMode_CallsConvertAndSendWithExchange() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.FANOUT, "", "test-exchange", "");

        // 执行
        sender.send(message);

        // 验证
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test payload"));
    }

    @Test
    void send_TopicMode_CallsConvertAndSendWithExchangeAndRoutingKey() {
        // 准备
        ITransactionMessage<Object> message = createTestMessage(MessageDeliveryMode.TOPIC, "", "test-exchange", "test.routing.key");

        // 执行
        sender.send(message);

        // 验证
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test.routing.key"), eq("test payload"));
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
