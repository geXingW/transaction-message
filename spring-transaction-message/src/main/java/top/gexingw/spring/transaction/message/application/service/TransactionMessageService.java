package top.gexingw.spring.transaction.message.application.service;

import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

import java.io.Serializable;
import java.util.List;

/**
 * @author GeXingW
 */
public interface TransactionMessageService {

    List<TransactionMessage> queryRetryableMessages();

    List<TransactionMessage> queryRetryableMessages(long startTimestamp);

    void sendSucceed(Serializable id);

    void sendFailed(Serializable id);

    <Payload> void send(ITransactionMessage<Payload> transactionMessage, Runnable sendCallback);

    <Payload> void send(ITransactionMessage<Payload> transactionMessage);

    /**
     * 查询当前到达时间的消息，并进行发送
     */
    void run();

}
