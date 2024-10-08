package top.gexingw.spring.transaction.message.infrastructure.config;


import top.gexingw.spring.transaction.message.infrastructure.repository.JdbcTransactionMessageRepositoryImpl;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageRepository;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.application.service.impl.JdbcTransactionMessageServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * @author GeXingW
 */
@AutoConfiguration
@AutoConfigureAfter({JdbcOperations.class})
public class TransactionMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionMessageService transactionMessageService(TransactionMessageRepository transactionMessageRepository) {
        return new JdbcTransactionMessageServiceImpl(transactionMessageRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMessageRepository transactionMessageRepository(JdbcOperations jdbcOperations) {
        return new JdbcTransactionMessageRepositoryImpl(jdbcOperations);
    }

}
