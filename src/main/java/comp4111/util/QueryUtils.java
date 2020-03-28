package comp4111.util;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
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
    public static <T> List<T> queryTable(@NotNull final Connection con, @NotNull String tableName,
                                         @NotNull String ext, @NotNull List<Object> params,
                                         @NotNull Function<ResultSet, T> transform) throws SQLException {
        final var list = new ArrayList<T>();
        try (
                // https://www.w3schools.com/sql/sql_select.asp
                PreparedStatement stmt = ext.equals("") ? con.prepareStatement("select * from " + tableName) :
                        con.prepareStatement("select * from " + tableName + " " + ext)
        ) {
            final ResultSet rs;
            if (ext.equals("")) {
                rs = stmt.executeQuery();
            } else {
                int index = 1;
                for (Object p : params) {
                    if (p instanceof Long) {
                        stmt.setLong(index++, (Long) p);
                    } else if (p instanceof String) {
                        stmt.setString(index++, (String) p);
                    }
                }
                rs = stmt.executeQuery();
            }
            while (rs.next()) {
                list.add(transform.apply(rs));
            }
        }
        return list;
    }
}
