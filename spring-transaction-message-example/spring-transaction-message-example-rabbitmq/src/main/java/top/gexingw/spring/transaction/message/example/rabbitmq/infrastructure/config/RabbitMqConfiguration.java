package top.gexingw.spring.transaction.message.example.rabbitmq.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.example.rabbitmq.infrastructure.util.JacksonUtil;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;
import top.gexingw.spring.transaction.message.rabbitmq.RabbitMQConfirmCallback;
import top.gexingw.spring.transaction.message.rabbitmq.RabbitMQTransactionMessageSender;

/**
 * @author GeXingW
 */
@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class RabbitMqConfiguration {

    private final TransactionMessageService transactionMessageService;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();

        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());

        rabbitTemplate.setConfirmCallback(new RabbitMQConfirmCallback(transactionMessageService));
        rabbitTemplate.setConfirmCallback(new RabbitMQConfirmCallback(transactionMessageService));
        rabbitTemplate.setReturnsCallback(new ReturnCallback());

        rabbitTemplate.setMandatory(true);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(2000);



        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public static class ReturnCallback implements RabbitTemplate.ReturnsCallback {

        @Override
        public void returnedMessage(@NotNull ReturnedMessage returnedMessage) {
            log.error("消息被退回，内容：{}", JacksonUtil.toJson(returnedMessage));
        }
    }

    @Bean
    public TransactionMessageSender transactionMessageSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMQTransactionMessageSender(rabbitTemplate);
    }

//    @AllArgsConstructor
//    public static class ConfirmCallback implements RabbitTemplate.ConfirmCallback {
//
//        private final TransactionMessageService transactionMessageService;
//
//        @Override
//        public void confirm(CorrelationData correlationData, boolean ack, @Nullable String cause) {
//            if (ack) {
//                log.info("消息发送成功，内容：{}", JacksonUtil.toJson(correlationData));
//                transactionMessageService.sendSucceed(correlationData.getId());
//                log.info("消息发送成功，已移除：{}", JacksonUtil.toJson(correlationData));
//            } else {
//                log.error("消息发送失败，内容：{}，原因：{}", JacksonUtil.toJson(correlationData), cause);
//                transactionMessageService.sendFailed(correlationData.getId());
//            }
//
//        }
//    }
}
