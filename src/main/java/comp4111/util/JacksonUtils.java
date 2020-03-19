package comp4111.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

public class JacksonUtils {

    private JacksonUtils() {
    }

    /**
     * @return An {@link ObjectMapper} which is configured for this project.
     */
    @NotNull
    public static ObjectMapper getDefaultObjectMapper() {
        // currently there is no setup.
        // if say, we want to allow case-insensitive properties, we will add it here.
        return new ObjectMapper();
    }
}
