package comp4111.function;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A {@link java.util.function.Function} which throws a {@link SQLException}.
 */
public interface ConnectionFunction<R> {

    /**
     * Applies this function to a given {@link Connection}.
     *
     * @param connection The SQL connection.
     * @return The function result.
     * @throws SQLException if the database operation fails.
     * @see java.util.function.Function#apply(Object)
     */
    R apply(@NotNull Connection connection) throws SQLException;
}
