package top.gexingw.spring.transaction.message.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Lazy;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;

@EnableAutoConfiguration
@ConditionalOnBean(RabbitTemplate.class)
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class SpringTransactionMessageRabbitMQAutoConfiguration {

    @ConditionalOnMissingBean(TransactionMessageSender.class)
    public TransactionMessageSender transactionMessageSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMQTransactionMessageSender(rabbitTemplate);
    }

}
