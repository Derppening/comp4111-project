package comp4111.listener;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

public class GenericExceptionListener implements ExceptionListener {

    public final static GenericExceptionListener INSTANCE = new GenericExceptionListener();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onError(Exception ex) {
        if (ex instanceof SocketException) {
            logger.debug(ex.getMessage());
        } else {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void onError(HttpConnection connection, Exception ex) {
        if (ex instanceof ConnectionClosedException) {
            logger.debug(ex.getMessage());
        } else if (ex instanceof SocketException) {
            logger.debug(ex.getMessage());
        } else {
            logger.error(ex.getMessage(), ex);
        }
    }
}
