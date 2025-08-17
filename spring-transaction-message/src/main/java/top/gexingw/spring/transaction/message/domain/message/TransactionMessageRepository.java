package top.gexingw.spring.transaction.message.domain.message;

import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

import java.io.Serializable;
import java.util.List;

/**
 * @author GeXingW
 */
public interface TransactionMessageRepository {

    List<TransactionMessage> queryAllRetryable(long currentTimestamp);

    <Payload> void insert(ITransactionMessage<Payload> _transactionMessage);

    void update(TransactionMessage transactionMessage);

    void remove(Serializable id);

    TransactionMessage find(Serializable id);
    
}
