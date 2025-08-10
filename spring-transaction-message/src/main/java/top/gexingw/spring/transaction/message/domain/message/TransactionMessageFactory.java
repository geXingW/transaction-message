package top.gexingw.spring.transaction.message.domain.message;

import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.util.JacksonUtil;

/**
 * 事务消息工厂类，负责创建事务消息领域对象
 *
 * @author GeXingW
 */
public class TransactionMessageFactory {

    /**
     * 将ITransactionMessage接口转换为TransactionMessage领域对象
     *
     * @param transactionMessage 事务消息接口
     * @param <Payload>          消息载荷类型
     * @return TransactionMessage领域对象
     */
    public static <Payload> TransactionMessage createFrom(ITransactionMessage<Payload> transactionMessage) {
        // 创建领域对象
        TransactionMessage domainMessage = new TransactionMessage();

        // 设置基本属性
        Long messageId = transactionMessage.getId() != null ? transactionMessage.getId() : System.currentTimeMillis();
        domainMessage.setId(messageId);
        domainMessage.setExchange(transactionMessage.getExchange());
        domainMessage.setRoutingKey(transactionMessage.getRoutingKey());
        domainMessage.setQueue(transactionMessage.getQueue());

        // 设置重试相关属性
        domainMessage.setMaxRetryCount(transactionMessage.getMaxRetryCount());
        domainMessage.setRetriedCount(0); // 初始重试次数为0
        domainMessage.setNextRetryTime(System.currentTimeMillis()); // 设置下次重试时间为当前时间

        // 设置消息载荷，将原始对象序列化为JSON字符串
        domainMessage.setPayload(JacksonUtil.toJson(transactionMessage.getPayload()));

        // 设置消息状态为正常
        domainMessage.setSendStatus(MessageSendStatus.NORMAL);

        // 设置消息投递模式
        domainMessage.setDeliveryMode(transactionMessage.getDeliveryMode());

        return domainMessage;
    }
}
