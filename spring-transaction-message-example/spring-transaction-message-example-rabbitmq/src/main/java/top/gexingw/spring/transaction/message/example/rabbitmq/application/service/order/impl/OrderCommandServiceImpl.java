package top.gexingw.spring.transaction.message.example.rabbitmq.application.service.order.impl;

import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.gexingw.spring.transaction.message.example.rabbitmq.application.service.order.OrderCommandService;
import top.gexingw.spring.transaction.message.example.rabbitmq.domain.order.OrderCreatedITransactionMessage;

/**
 * @author GeXingW
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class OrderCommandServiceImpl implements OrderCommandService {

    private final TransactionMessageService transactionMessageService;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void create(Long id) {
        OrderCreatedITransactionMessage orderCreatedMessage = new OrderCreatedITransactionMessage(1L, 2L);
        transactionMessageService.send(orderCreatedMessage);
    }

}
