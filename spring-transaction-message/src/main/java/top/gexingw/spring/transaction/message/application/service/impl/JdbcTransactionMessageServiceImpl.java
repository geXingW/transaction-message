package top.gexingw.spring.transaction.message.application.service.impl;

import org.springframework.transaction.annotation.Transactional;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.domain.message.MessageSendStatus;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageRepository;
import top.gexingw.spring.transaction.message.infrastructure.config.TransactionMessageConfigProperties;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;
import top.gexingw.spring.transaction.message.infrastructure.util.TransactionUtil;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * @author GeXingW
 */
public class JdbcTransactionMessageServiceImpl implements TransactionMessageService {

    private final TransactionMessageRepository transactionMessageRepository;
    private final TransactionMessageSender transactionMessageSender;
    private final TransactionMessageConfigProperties transactionMessageConfigProperties;

    public JdbcTransactionMessageServiceImpl(
            TransactionMessageRepository transactionMessageRepository, TransactionMessageSender transactionMessageSender
            , TransactionMessageConfigProperties transactionMessageConfigProperties
    ) {
        this.transactionMessageRepository = transactionMessageRepository;
        this.transactionMessageSender = transactionMessageSender;
        this.transactionMessageConfigProperties = transactionMessageConfigProperties;
    }

    @Override
    public List<TransactionMessage> queryRetryableMessages() {
        return queryRetryableMessages(Instant.now().getEpochSecond());
    }

    @Override
    public List<TransactionMessage> queryRetryableMessages(long startTimestamp) {
        return transactionMessageRepository.queryAllRetryable(startTimestamp);
    }

    @Override
    public void sendSucceed(Serializable id) {
        TransactionMessage transactionMessage = transactionMessageRepository.find(id);
        transactionMessage.setSendStatus(MessageSendStatus.SUCCEED);

        transactionMessageRepository.remove(id);
    }

    @Override
    public void sendFailed(Serializable id) {
        TransactionMessage transactionMessage = transactionMessageRepository.find(id);

        // 当前重试次数
        int retriedCount = transactionMessage.getRetriedCount() == null ? 0 : transactionMessage.getRetriedCount();
        // 如果达到最大重试次数，不再重试；状态改为失败
        if (retriedCount >= transactionMessageConfigProperties.getMaxRetryCount()) {
            transactionMessage.setSendStatus(MessageSendStatus.FAILED);
            transactionMessageRepository.save(transactionMessage);
            return;
        }

        // 下次重试时间为当前时间 + 重试间隔
        transactionMessage.setRetriedCount(++retriedCount);
        // 下次重试时间为当前时间 + 重试间隔
        long nextRetryTime = Instant.now().plus(transactionMessageConfigProperties.getRetryInterval()).getEpochSecond();
        transactionMessage.setNextRetryTime(nextRetryTime);

        transactionMessageRepository.save(transactionMessage);
    }

    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        transactionMessageRepository.save(transactionMessage);

        TransactionUtil.doAfterCommitted(() -> {
            transactionMessageSender.send(transactionMessage);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage, Runnable sendCallback) {
        transactionMessageRepository.save(transactionMessage);

        TransactionUtil.doAfterCommitted(() -> {
            transactionMessageSender.send(transactionMessage);
        });
    }

}
