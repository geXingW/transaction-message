package top.gexingw.spring.transaction.message.application.service;

import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * @author GeXingW
 */
public interface TransactionMessageService {

    List<TransactionMessage> queryRetryableMessages();

    List<TransactionMessage> queryRetryableMessages(long currentTimestamp);

    void sendSucceed(Serializable id);

    void sendFailed(Serializable id);

    @Transactional(rollbackFor = Exception.class)
    void send(TransactionMessage transactionMessage, Runnable sendCallback);

    <Payload> void send(ITransactionMessage<Payload> transactionMessage);

}
