package top.gexingw.spring.transaction.message.domain.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * 消息发送模式枚举
 * 
 * @author GeXingW
 */
public enum MessageDeliveryMode implements Serializable {

    /**
     * 简单模式：直接发送到队列，不经过交换机
     */
    
    SIMPLE("simple"),

    /**
     * 扇出模式：发送到交换机，广播给所有绑定的队列，忽略路由键
     */
    FANOUT("fanout"),

    /**
     * 主题模式：发送到交换机，根据路由键匹配规则路由到相应队列
     */
    TOPIC("topic"),

    /**
     * 直连模式：发送到交换机，根据完全匹配的路由键发送到队列
     */
    DIRECT("direct");
    
    private final String value;
    
    MessageDeliveryMode(String value) {
        this.value = value;
    }
    
    /**
     * 获取枚举值的字符串表示
     * 
     * @return 字符串表示
     */
    @JsonValue
    public String getValue() {
        return this.value;
    }

    /**
     * 从字符串转换为枚举值
     * 
     * @param mode 模式字符串
     * @return 枚举值
     */
    @JsonCreator
    public static MessageDeliveryMode of(String mode) {
        if (mode == null || mode.isEmpty()) {
            return SIMPLE;
        }
        try {
            return MessageDeliveryMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SIMPLE; // 默认返回SIMPLE模式
        }
    }
    
    /**
     * 从字符串值转换为枚举
     * 
     * @param value 字符串值
     * @return 枚举值
     */
    public static MessageDeliveryMode fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return SIMPLE;
        }
        
        for (MessageDeliveryMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        
        return SIMPLE; // 默认返回SIMPLE模式
    }
    
    /**
     * 判断是否需要交换机
     * 
     * @return 是否需要交换机
     */
    public boolean requiresExchange() {
        return this != SIMPLE;
    }
    
    /**
     * 判断是否需要路由键
     * 
     * @return 是否需要路由键
     */
    public boolean requiresRoutingKey() {
        return this == TOPIC || this == DIRECT;
    }
}
