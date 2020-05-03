package comp4111.listener;

import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

public class GenericExceptionCallback implements Callback<Exception> {

    public final static GenericExceptionCallback INSTANCE = new GenericExceptionCallback();

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(Exception object) {
        if (object instanceof SocketTimeoutException) {
            LOGGER.error("Connection timed out", object);
        } else if (object instanceof ConnectionClosedException) {
            LOGGER.trace("", object);
        } else {
            LOGGER.error("Unknown exception occurred.", object);
        }
    }
}
