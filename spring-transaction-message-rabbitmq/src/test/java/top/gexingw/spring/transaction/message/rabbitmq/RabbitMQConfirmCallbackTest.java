package top.gexingw.spring.transaction.message.rabbitmq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;

import static org.mockito.Mockito.*;

class RabbitMQConfirmCallbackTest {

    @Mock
    private TransactionMessageService transactionMessageService;

    private RabbitMQConfirmCallback callback;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        callback = new RabbitMQConfirmCallback(transactionMessageService);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void confirm_AckTrue_CallsSendSucceed() {
        // 准备
        CorrelationData correlationData = new CorrelationData("1");

        // 执行
        callback.confirm(correlationData, true, null);

        // 验证
        verify(transactionMessageService).sendSucceed("1");
        verify(transactionMessageService, never()).sendFailed(any());
    }

    @Test
    void confirm_AckFalse_CallsSendFailed() {
        // 准备
        CorrelationData correlationData = new CorrelationData("1");

        // 执行
        callback.confirm(correlationData, false, "test cause");

        // 验证
        verify(transactionMessageService).sendFailed("1");
        verify(transactionMessageService, never()).sendSucceed(any());
    }
}
