package comp4111.exception;

/**
 * An exception indicating that an error has occurred while processing an HTTP request.
 */
public class HttpHandlingException extends Exception {

    private final int httpStatus;

    public HttpHandlingException(int httpStatus) {
        super();
        this.httpStatus = httpStatus;
    }

    public HttpHandlingException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpHandlingException(int httpStatus, Throwable cause) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    public HttpHandlingException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * @return The HTTP code to return.
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
