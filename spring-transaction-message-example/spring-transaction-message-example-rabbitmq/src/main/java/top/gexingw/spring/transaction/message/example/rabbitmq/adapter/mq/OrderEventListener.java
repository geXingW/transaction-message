package top.gexingw.spring.transaction.message.example.rabbitmq.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author GeXingW
 */
@Slf4j
@Component
public class OrderEventListener {

    @RabbitListener(queues = "order-queue")
    public void onOrderEvent(Object payload) {
//        log.info("收到订单消息！{}", JacksonUtil.toJson(payload));
        log.info("收到订单消息={}", payload);
    }

}
