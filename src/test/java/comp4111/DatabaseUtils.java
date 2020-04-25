package comp4111;

import java.sql.DriverManager;
import java.sql.Statement;

import static comp4111.dal.DatabaseConnection.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DatabaseUtils {

    private DatabaseUtils() {
    }

    public static void dropDatabase(String dbName) {
        try {
            final var con = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("DROP DATABASE " + dbName);
            }
        } catch (Exception e) {
            assumeTrue(false);
        }
    }
}
