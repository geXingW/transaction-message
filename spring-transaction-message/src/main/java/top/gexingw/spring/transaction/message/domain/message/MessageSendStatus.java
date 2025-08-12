package top.gexingw.spring.transaction.message.domain.message;

/**
 * @author GeXingW
 */
public enum MessageSendStatus {

    NORMAL("NORMAL"),

    FAILED("FAILED"),

    SUCCEED("SUCCEED")

    ;

    private final String value;

    MessageSendStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static MessageSendStatus of(String status) {
        return MessageSendStatus.valueOf(status);
    }

}
