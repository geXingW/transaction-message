package top.gexingw.spring.transaction.message.rabbitmq;

import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;

public class RabbitMQConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private final TransactionMessageService transactionMessageService;

    public RabbitMQConfirmCallback(TransactionMessageService transactionMessageService) {
        this.transactionMessageService = transactionMessageService;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, @Nullable String cause) {
        // correlationData为null时不处理
        if (correlationData == null) {
            return;
        }
        
        if (ack) {
            transactionMessageService.sendSucceed(correlationData.getId());
            return;
        }

        transactionMessageService.sendFailed(correlationData.getId());
    }

}
