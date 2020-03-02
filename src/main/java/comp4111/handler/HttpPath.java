package comp4111.handler;

import org.jetbrains.annotations.NotNull;

public interface HttpPath {

    @NotNull
    String getHandlePattern();
}
