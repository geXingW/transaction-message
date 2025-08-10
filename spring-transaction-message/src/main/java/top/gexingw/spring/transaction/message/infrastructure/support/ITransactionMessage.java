package top.gexingw.spring.transaction.message.infrastructure.support;

import org.jetbrains.annotations.Nullable;
import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;

public interface ITransactionMessage<Payload> {

    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    Long getId();

    /**
     * 获取消息体
     *
     * @return 消息体
     */
    Payload getPayload();

    /**
     * 获取指定交换机名称
     *
     * @return 交换机名称
     */
    default String getExchange() {
        return "";
    }

    /**
     * 获取路由键
     *
     * @return 路由键
     */
    default String getRoutingKey() {
        return "";
    }

    /**
     * 获取队列名称
     *
     * @return 队列名称
     */
    default String getQueue() {
        return "";
    }

    /**
     * 获取消息发送模式
     *
     * @return 消息发送模式
     *
     * @see MessageDeliveryMode
     */
    MessageDeliveryMode getDeliveryMode();

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    default @Nullable Integer getMaxRetryCount() {
        return null;
    }

}
