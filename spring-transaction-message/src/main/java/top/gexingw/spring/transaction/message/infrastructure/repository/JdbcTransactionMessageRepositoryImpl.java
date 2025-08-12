package top.gexingw.spring.transaction.message.infrastructure.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.jdbc.core.JdbcOperations;
import top.gexingw.spring.transaction.message.domain.message.MessageSendStatus;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageFactory;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageRepository;
import top.gexingw.spring.transaction.message.infrastructure.config.TransactionMessageConfigProperties;
import top.gexingw.spring.transaction.message.infrastructure.convert.MessageConverter;
import top.gexingw.spring.transaction.message.infrastructure.support.ITransactionMessage;

import java.io.Serializable;
import java.sql.Types;
import java.util.List;

/**
 * @author GeXingW
 */
public class JdbcTransactionMessageRepositoryImpl implements TransactionMessageRepository {

    private final static String INSERT_SQL = "INSERT INTO transaction_message " +
            "(id, exchange_name, routing_key, queue_name, max_retry_count, next_retry_time, payload, send_status, delivery_mode) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcOperations jdbcOperations;
    private final TransactionMessageConfigProperties configProperties;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public JdbcTransactionMessageRepositoryImpl(JdbcOperations jdbcOperations, TransactionMessageConfigProperties configProperties) {
        this.jdbcOperations = jdbcOperations;
        this.configProperties = configProperties;
    }

    @Override
    public List<TransactionMessage> queryAllRetryable(long currentTimestamp) {
        String querySql = "select * from transaction_message where next_retry_time <= ? and send_status = ?";
        Object[] args = {currentTimestamp, MessageSendStatus.NORMAL.getValue()};
        int[] argTypes = {Types.INTEGER, Types.CHAR};

        return jdbcOperations.query(
                querySql, args, argTypes, (rs, rowNum) -> MessageConverter.toTransactionMessage(rs)
        );
    }

    @Override
    public void save(TransactionMessage transactionMessage) {
        if (transactionMessage.getId() == null) {
            this.create(transactionMessage);
            return;
        }

        this.update(transactionMessage);
    }

    @Override
    public <Payload> void save(ITransactionMessage<Payload> _transactionMessage) {
        // 创建新消息
        TransactionMessage transactionMessage = TransactionMessageFactory.createFrom(_transactionMessage);
        // 最大重试次数
        Integer maxRetryCount = transactionMessage.getMaxRetryCount() != null ? transactionMessage.getMaxRetryCount() : configProperties.getMaxRetryCount();

        Object[] args = {
                transactionMessage.getId(), transactionMessage.getExchange(), transactionMessage.getRoutingKey(), transactionMessage.getQueue()
                , maxRetryCount, transactionMessage.getRetriedCount(), transactionMessage.getPayload(), transactionMessage.getSendStatus().getValue()
                , transactionMessage.getDeliveryMode().getValue()
        };

        if (jdbcOperations.update(INSERT_SQL, args) <= 0) {
            throw new RuntimeException("保存事务消息失败");
        }
    }

    public <Payload> void update(ITransactionMessage<Payload> transactionMessage) {
        // 待实现
    }

    public void create(TransactionMessage transactionMessage) {
        long id = System.currentTimeMillis();

        // 使用消息中的最大重试次数，如果为null则使用配置中的默认值
        Integer maxRetryCount = transactionMessage.getMaxRetryCount() != null ? transactionMessage.getMaxRetryCount() : configProperties.getMaxRetryCount();

        Object[] args = {
                id, transactionMessage.getExchange(), transactionMessage.getRoutingKey(), transactionMessage.getQueue(), maxRetryCount
                , transactionMessage.getNextRetryTime(), transactionMessage.getPayload(), MessageSendStatus.NORMAL.getValue()
                , transactionMessage.getDeliveryMode().getValue()
        };
        if (jdbcOperations.update(INSERT_SQL, args) <= 0) {
            throw new RuntimeException("保存事务消息失败");
        }

        transactionMessage.setId(id);
    }

    private void update(TransactionMessage transactionMessage) {
        String sql = "UPDATE transaction_message set retried_count = ?, next_retry_time = ?, send_status = ? WHERE id = ?";
        Object[] args = {
                transactionMessage.getRetriedCount(), transactionMessage.getNextRetryTime(), transactionMessage.getSendStatus().getValue()
                , transactionMessage.getId()
        };
        if (jdbcOperations.update(sql, args) <= 0) {
            throw new RuntimeException("更新事务消息失败");
        }
    }

    @Override
    public void remove(Serializable id) {
        if (jdbcOperations.update("DELETE FROM transaction_message WHERE id = ?", id) <= 0) {
            throw new RuntimeException("删除事务消息失败");
        }
    }

    @Override
    public TransactionMessage find(Serializable id) {
        Object[] args = {id};
        int[] argTypes = {Types.BIGINT};
        String query = "SELECT * FROM transaction_message WHERE id = ?";
        List<TransactionMessage> queryResult = jdbcOperations.query(query, args, argTypes, (rs, rowNum) -> MessageConverter.toTransactionMessage(rs));

        return !queryResult.isEmpty() ? queryResult.get(0) : null;
    }

}
