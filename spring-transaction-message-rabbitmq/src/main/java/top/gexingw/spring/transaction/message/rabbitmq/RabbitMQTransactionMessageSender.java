package top.gexingw.spring.transaction.message.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;

@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class RabbitMQTransactionMessageSender implements TransactionMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        // 创建CorrelationData，用于消息确认回调
        CorrelationData correlationData = null;
        if (transactionMessage.getId() != null) {
            correlationData = new CorrelationData(transactionMessage.getId().toString());
        }
        
        // 简单模式
        if (MessageDeliveryMode.SIMPLE.equals(transactionMessage.getDeliveryMode())) {
            rabbitTemplate.convertAndSend(transactionMessage.getQueue(), transactionMessage.getPayload(), correlationData);
        }

        // 扇出模式
        if (MessageDeliveryMode.FANOUT.equals(transactionMessage.getDeliveryMode())) {
            rabbitTemplate.convertAndSend(transactionMessage.getExchange(), "", transactionMessage.getPayload(), correlationData);
        }

        // 主题模式
        if (MessageDeliveryMode.TOPIC.equals(transactionMessage.getDeliveryMode())) {
            rabbitTemplate.convertAndSend(transactionMessage.getExchange(), transactionMessage.getRoutingKey(), transactionMessage.getPayload(), correlationData);
        }
    }

}
