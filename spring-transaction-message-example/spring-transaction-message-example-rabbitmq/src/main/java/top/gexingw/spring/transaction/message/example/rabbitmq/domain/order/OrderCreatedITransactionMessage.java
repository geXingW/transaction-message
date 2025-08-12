package top.gexingw.spring.transaction.message.example.rabbitmq.domain.order;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class OrderCreatedITransactionMessage implements ITransactionMessage<OrderCreatedITransactionMessage.Payload> {

    private final String exchange = "order.exchange";

    private final String routingKey = "order.created";

    private final MessageDeliveryMode deliveryMode = MessageDeliveryMode.TOPIC;

    private Long id;

    private Payload payload;

    @Data
    @Accessors(chain = true)
    public static class Payload implements Serializable {

        private Long orderId;

        private Long userId;

    }

    public OrderCreatedITransactionMessage(Long orderId, Long userId) {
        this.payload = new Payload().setOrderId(orderId).setUserId(userId);
    }

}
