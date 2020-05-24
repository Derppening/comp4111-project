package comp4111.dal;

import org.jetbrains.annotations.NotNull;

/**
 * Helper enum representing the available lock modes of InnoDB.
 */
public enum InnoDBLockMode {
    DEFAULT,
    SHARE,
    UPDATE;

    @NotNull
    public String asSQLQueryComponent() {
        switch (this) {
            case DEFAULT:
                return "";
            case SHARE:
                return "LOCK IN SHARE MODE";
            case UPDATE:
                return "FOR UPDATE";
        }

        throw new IllegalStateException();
    }
}
