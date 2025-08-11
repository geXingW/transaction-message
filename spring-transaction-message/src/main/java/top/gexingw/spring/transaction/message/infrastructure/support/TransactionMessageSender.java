package top.gexingw.spring.transaction.message.infrastructure.support;

public interface TransactionMessageSender {

    <Payload> void send(ITransactionMessage<Payload> transactionMessage);

}
