package comp4111.handler;

import org.jetbrains.annotations.NotNull;

/**
 * A path on the HTTP server.
 */
public interface HttpPath {

    /**
     * Prefix to all paths of this server.
     */
    String PATH_PREFIX = "/BookManagementService";

    /**
     * @return The path pattern that this object handles.
     */
    @NotNull
    String getHandlePattern();
}
