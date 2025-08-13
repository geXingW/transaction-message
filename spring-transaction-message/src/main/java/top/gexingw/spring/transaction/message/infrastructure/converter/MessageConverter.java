package top.gexingw.spring.transaction.message.infrastructure.converter;

import top.gexingw.spring.transaction.message.domain.message.MessageDeliveryMode;
import top.gexingw.spring.transaction.message.domain.message.MessageSendStatus;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageConverter {

    public static TransactionMessage toTransactionMessage(ResultSet resultSet) throws SQLException {
        TransactionMessage transactionMessage = new TransactionMessage();
        transactionMessage.setId(resultSet.getLong("id"));
        transactionMessage.setExchange(resultSet.getString("exchange_name"));
        transactionMessage.setRoutingKey(resultSet.getString("routing_key"));
        transactionMessage.setQueue(resultSet.getString("queue_name"));
        transactionMessage.setMaxRetryCount(resultSet.getInt("max_retry_count"));
        transactionMessage.setRetriedCount(resultSet.getInt("retried_count"));
        transactionMessage.setNextRetryTime(resultSet.getLong("next_retry_time"));
        transactionMessage.setPayload(resultSet.getString("payload"));
        transactionMessage.setSendStatus(MessageSendStatus.of(resultSet.getString("send_status")));
        transactionMessage.setDeliveryMode(MessageDeliveryMode.of(resultSet.getString("delivery_mode")));
//        transactionMessage.setMeta(resultSet.getString("meta"));

        return transactionMessage;
    }

}
