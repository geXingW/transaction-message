package top.gexingw.spring.transaction.message.infrastructure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import top.gexingw.spring.transaction.message.application.service.TransactionMessageService;
import top.gexingw.spring.transaction.message.application.service.impl.JdbcTransactionMessageServiceImpl;
import top.gexingw.spring.transaction.message.domain.message.TransactionMessageRepository;
import top.gexingw.spring.transaction.message.infrastructure.repository.JdbcTransactionMessageRepositoryImpl;
import top.gexingw.spring.transaction.message.infrastructure.support.TransactionMessageSender;

/**
 * @author GeXingW
 */
@AutoConfiguration
@AutoConfigureAfter({JdbcOperations.class})
@EnableConfigurationProperties(TransactionMessageConfigProperties.class)
public class TransactionMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionMessageService transactionMessageService(
            TransactionMessageRepository transactionMessageRepository, TransactionMessageSender transactionMessageSender
            , TransactionMessageConfigProperties transactionMessageConfigProperties
    ) {
        return new JdbcTransactionMessageServiceImpl(
                transactionMessageRepository, transactionMessageSender, transactionMessageConfigProperties
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMessageRepository transactionMessageRepository(JdbcOperations jdbcOperations, TransactionMessageConfigProperties properties) {
        return new JdbcTransactionMessageRepositoryImpl(jdbcOperations, properties);
    }

}
