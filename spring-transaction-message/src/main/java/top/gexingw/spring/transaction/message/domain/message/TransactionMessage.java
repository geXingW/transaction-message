package top.gexingw.spring.transaction.message.domain.message;

import org.jetbrains.annotations.Nullable;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

/**
 * @author GeXingW
 */
public final class TransactionMessage implements ITransactionMessage<String> {

    private Long id;

    private String payload;

    private String exchange;

    private String routingKey;

    private String queue;

    private MessageDeliveryMode deliveryMode;

    private Integer maxRetryCount;

    private Integer retriedCount;

    private Long nextRetryTime;

    private MessageSendStatus sendStatus;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @Override
    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Override
    public MessageDeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(MessageDeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    @Override
    public @Nullable Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Integer getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
    }

    public MessageSendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(MessageSendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Long getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(Long nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

}
