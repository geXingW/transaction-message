package top.gexingw.spring.transaction.message.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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

    /**
     * 重试间隔时间
     */
    private Duration retryInterval = Duration.ofSeconds(3);

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Duration getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Duration retryInterval) {
        this.retryInterval = retryInterval;
    }

}
