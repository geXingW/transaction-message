package top.gexingw.spring.transaction.message.infrastructure.support;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.infrastructure.config.TransactionMessageConfigProperties;

@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class TransactionMessageTemplate {

    public final TransactionMessageService transactionMessageService;

    private final TransactionMessageConfigProperties transactionMessageConfigProperties;

    public <Payload> void run() {
        transactionMessageService.run();
    }

    public <Payload> void send(ITransactionMessage<Payload> transactionMessage) {
        transactionMessageService.send(transactionMessage);
    }

    public <Payload> void failed() {

    }

}
