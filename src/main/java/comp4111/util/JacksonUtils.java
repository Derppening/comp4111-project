package comp4111.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
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
        final var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);

        return objectMapper;
    }
}
