package top.gexingw.spring.transaction.message.application.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
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

    public static final Logger logger = LoggerFactory.getLogger(JdbcTransactionMessageServiceImpl.class);

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
        Assert.notNull(transactionMessage, "消息不存在");

        // 消息发送失败
        transactionMessage.sendFail(transactionMessageConfigProperties.getMaxRetryCount());
        transactionMessageRepository.update(transactionMessage);
    }

    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        this.send(transactionMessage, null);
    }

    @Override
    public <Payload> void send(ITransactionMessage<Payload> transactionMessage, Runnable sendCallback) {
        Long messageId = transactionMessage.getId();
        Assert.notNull(messageId, "消息ID不能为空");

        TransactionMessage existTransactionMessage = transactionMessageRepository.find(messageId);
        if (existTransactionMessage == null) {
            transactionMessageRepository.insert(transactionMessage);
            return;
        }

        // 消息发送失败
        existTransactionMessage.sendFail(transactionMessageConfigProperties.getMaxRetryCount());
        transactionMessageRepository.update(existTransactionMessage);

        // 如果当前没有开启事务，就走同步发送
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            logger.warn("当前未开启事务，消息将同步发送");
            transactionMessageSender.send(transactionMessage);
            logger.warn("当前未开启事务，消息已同步发送");

            if (sendCallback != null) {
                sendCallback.run();
            }
        } else {
            TransactionUtil.doAfterCommitted(() -> {
                logger.debug("事务消息已落库，准备立即发送到MQ");
                transactionMessageSender.send(transactionMessage);
                logger.debug("事务消息已落库，已经发送到MQ");
            });

            if (sendCallback != null) {
                TransactionUtil.doAfterCommitted(sendCallback);
            }
        }
    }

    @Override
    public void run() {
        long startTimestamp = Instant.now().getEpochSecond();
        List<TransactionMessage> transactionMessages = this.queryRetryableMessages(startTimestamp);
        for (TransactionMessage transactionMessage : transactionMessages) {
            this.send(transactionMessage);
        }
    }


}
