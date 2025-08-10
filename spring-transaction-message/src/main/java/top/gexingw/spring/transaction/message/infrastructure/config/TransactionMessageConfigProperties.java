package top.gexingw.spring.transaction.message.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 事务消息配置属性
 * 
 * @author GeXingW
 */
@ConfigurationProperties(prefix = "spring.transaction.message")
public class TransactionMessageConfigProperties {

    /**
     * 最大重试次数，默认为3次
     */
    private Integer maxRetryCount = 3;

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

}
