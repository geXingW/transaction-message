package top.gexingw.spring.transaction.message.domain.message;

import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

/**
 * 事务消息适配器，负责接口与实体之间的转换
 * 
 * @author GeXingW
 */
public class TransactionMessageAdapter {

    /**
     * 将接口转换为实体对象
     * 
     * @param message 消息接口
     * @return 消息实体
     */
    public static <Payload> TransactionMessage toEntity(ITransactionMessage<Payload> message) {
        if (message instanceof TransactionMessage) {
            return (TransactionMessage) message;
        }
        
        TransactionMessage entity = new TransactionMessage();
        entity.setId(message.getId());
        entity.setPayload(message.getPayload());
        entity.setExchange(message.getExchange());
        entity.setRoutingKey(message.getRoutingKey());
        entity.setQueue(message.getQueue());
        entity.setDeliveryMode(message.getDeliveryMode());
        entity.setMaxRetryCount(message.getMaxRetryCount());
        
        // 设置默认技术属性
        entity.setSendStatus(MessageSendStatus.NORMAL);
        entity.setRetriedCount(0);
        
        return entity;
    }

    /**
     * 更新实体的业务属性（保持技术属性不变）
     * 
     * @param entity 目标实体
     * @param message 源消息接口
     */
    public static <Payload> void updateBusinessProperties(TransactionMessage entity, ITransactionMessage<Payload> message) {
        entity.setPayload(message.getPayload());
        entity.setExchange(message.getExchange());
        entity.setRoutingKey(message.getRoutingKey());
        entity.setQueue(message.getQueue());
        entity.setDeliveryMode(message.getDeliveryMode());
        
        // 只在接口提供时才更新最大重试次数
        if (message.getMaxRetryCount() != null) {
            entity.setMaxRetryCount(message.getMaxRetryCount());
        }
    }
}
