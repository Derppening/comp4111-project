package comp4111.function;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFunction {

    Object accept(@NotNull Connection connection) throws SQLException;
}
