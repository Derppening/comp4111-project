package comp4111.util;

import comp4111.dal.DatabaseConnectionPoolV2;
import comp4111.function.ConnectionFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryUtils {

    /**
     * Queries a table with extra clauses, converting them into Java objects.
     *
     * @param con {@link Connection} to the database.
     * @param tableName Name of the table to query.
     * @param ext Extra clauses.
     * @param transform Transformation function to convert a {@link ResultSet} row into a Java object.
     * @param <T> Type of the object in Java.
     * @return {@link List} of rows, converted into Java objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> queryTable(@Nullable final Connection con, @NotNull String tableName,
                                         @NotNull String ext, @NotNull List<Object> params,
                                         @NotNull Function<ResultSet, T> transform) throws SQLException {
        final ConnectionFunction<List<T>> block = connection -> {
            final var list = new ArrayList<T>();

            @Language("SQL") final String query;
            if (!ext.isEmpty()) {
                query = "SELECT * FROM " + tableName + " " + ext;
            } else {
                query = "SELECT * FROM " + tableName;
            }

            final PreparedStatement stmt = connection.prepareStatement(query);
            try (stmt) {
                final ResultSet rs;
                if (!ext.isEmpty()) {
                    for (int i = 0; i < params.size(); ++i) {
                        final var idx = i + 1;
                        final var p = params.get(i);
                        if (p instanceof Long) {
                            stmt.setLong(idx, (Long) p);
                        } else if (p instanceof String) {
                            stmt.setString(idx, (String) p);
                        } else {
                            throw new IllegalArgumentException("Unknown conversion type");
                        }
                    }
                }

                rs = stmt.executeQuery();

                while (rs.next()) {
                    list.add(transform.apply(rs));
                }
            }
            return list;
        };

        if (con != null) {
            return block.apply(con);
        } else {
            return DatabaseConnectionPoolV2.getInstance().execStmt(block);
        }
    }
}
