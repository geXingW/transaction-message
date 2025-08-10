package top.gexingw.spring.transaction.message.application.service.impl;

import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.domain.message.MessageSendStatus;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageRepository;
import top.gexingw.spring.transaction.message.infrastructure.util.TransactionUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * @author GeXingW
 */
public class JdbcTransactionMessageServiceImpl implements TransactionMessageService {

    private final TransactionMessageRepository transactionMessageRepository;

    public JdbcTransactionMessageServiceImpl(TransactionMessageRepository transactionMessageRepository) {
        this.transactionMessageRepository = transactionMessageRepository;
    }

    @Override
    public List<TransactionMessage> queryRetryableMessages() {
        return queryRetryableMessages(Instant.now().getEpochSecond());
    }

    @Override
    public List<TransactionMessage> queryRetryableMessages(long currentTimestamp) {
        return transactionMessageRepository.queryAllRetryable(currentTimestamp);
    }

    @Override
    public void sendSucceed(Serializable id) {
        transactionMessageRepository.remove(id);
    }

    @Override
    public void sendFailed(Serializable id) {
        TransactionMessage transactionMessage = transactionMessageRepository.find(id);

        transactionMessage.setSendStatus(MessageSendStatus.FAILED);
        transactionMessageRepository.save(transactionMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(TransactionMessage transactionMessage, Runnable sendCallback) {
        transactionMessageRepository.save(transactionMessage);

        TransactionUtil.doAfterCommitted(sendCallback);
    }

    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        transactionMessageRepository.save(transactionMessage);
    }

    @Transactional(rollbackFor = Exception.class)
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage, Runnable sendCallback) {
        transactionMessageRepository.save(transactionMessage);
    }

}
