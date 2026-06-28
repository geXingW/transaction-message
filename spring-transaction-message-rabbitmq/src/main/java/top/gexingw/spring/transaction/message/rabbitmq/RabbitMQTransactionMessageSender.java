package top.gexingw.spring.transaction.message.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;

/**
 * RabbitMQ 事务消息发送器
 * <p>
 * 根据 {@link MessageDeliveryMode} 选择投递方式：
 * <ul>
 *     <li>SIMPLE：直接投递到队列</li>
 *     <li>FANOUT：投递到扇出交换机，忽略路由键</li>
 *     <li>TOPIC：投递到主题交换机，按路由键模式匹配</li>
 *     <li>DIRECT：投递到直连交换机，按路由键完全匹配</li>
 * </ul>
 *
 * @author GeXingW
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class RabbitMQTransactionMessageSender implements TransactionMessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 根据消息投递模式将消息发送到 RabbitMQ。
     *
     * @param transactionMessage 待发送的事务消息
     * @param <Payload>          消息载荷类型
     * @throws IllegalStateException 当出现未支持的投递模式时抛出，避免消息静默丢失
     */
    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        // 创建CorrelationData，用于消息确认回调
        CorrelationData correlationData = null;
        if (transactionMessage.getId() != null) {
            correlationData = new CorrelationData(transactionMessage.getId().toString());
        }

        MessageDeliveryMode mode = transactionMessage.getDeliveryMode();
        String exchange = transactionMessage.getExchange();
        String routingKey = transactionMessage.getRoutingKey();
        String queue = transactionMessage.getQueue();
        Object messageId = transactionMessage.getId();

        switch (mode) {
            // 简单模式：直接发送到队列，不经过交换机
            case SIMPLE:
                log.debug("Sending SIMPLE message, queue={}, messageId={}", queue, messageId);
                rabbitTemplate.convertAndSend(queue, transactionMessage.getPayload(), correlationData);
                break;

            // 扇出模式：广播到所有绑定队列，路由键被忽略
            case FANOUT:
                log.debug("Sending FANOUT message, exchange={}, messageId={}", exchange, messageId);
                rabbitTemplate.convertAndSend(exchange, "", transactionMessage.getPayload(), correlationData);
                break;

            // 主题模式：按路由键模式匹配
            case TOPIC:
                log.debug("Sending TOPIC message, exchange={}, routingKey={}, messageId={}", exchange, routingKey, messageId);
                rabbitTemplate.convertAndSend(exchange, routingKey, transactionMessage.getPayload(), correlationData);
                break;

            // 直连模式：通过 exchange + routingKey 完全匹配路由到目标队列
            case DIRECT:
                log.debug("Sending DIRECT message, exchange={}, routingKey={}, messageId={}", exchange, routingKey, messageId);
                rabbitTemplate.convertAndSend(exchange, routingKey, transactionMessage.getPayload(), correlationData);
                break;

            default:
                // 未知模式，显式抛异常避免消息静默丢失
                throw new IllegalStateException("Unsupported delivery mode: " + mode);
        }
    }

}
