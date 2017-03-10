package int_.esa.eo.ngeo.downloadmanager.exception;

/**
* <p>
* This exception is used to indicate an unexpected response when requesting a 
* product download. Typically this will be thrown when an HTTP response code is
* provided which is not expected from a Product Facility (e.g. 301, 302).
* </p>
* 
*/
public class UnexpectedResponseException extends DMPluginException {
    private static final long serialVersionUID = 1876996386126979776L;

    /**
     * Constructs an UnexpectedResponseException with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public UnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an UnexpectedResponseException with the specified cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public UnexpectedResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an UnexpectedResponseException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public UnexpectedResponseException(String message) {
        super(message);
    }

}
