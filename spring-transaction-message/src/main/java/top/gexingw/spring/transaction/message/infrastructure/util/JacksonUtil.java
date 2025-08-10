package top.gexingw.spring.transaction.message.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;

/**
 * @author GeXingW
 */
public class JacksonUtil {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    @NotNull
    public static String toJson(@NotNull Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static <T> T toObject(@NotNull String json, @NotNull Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static <T> T toObject(@NotNull String json, @NotNull Class<T> clazz, Class<?>... parameterClasses) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructParametricType(clazz, parameterClasses));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
