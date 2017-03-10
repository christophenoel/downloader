package int_.esa.eo.ngeo.downloadmanager.exception;

/**
 * <p>
 * This exception is used to indicate a general exception that has occurred in a
 * Download Manager Plugin that cannot be rectified without further modification
 * and restart of the application. This includes unable to load configuration,
 * unable to parse files received, etc.
 * </p>
 * 
 * <p>
 * Subclasses of this exception provide a more specific type of exception in order
 * to provide more detail to the exception.
 * </p>
 * 
 * <p>The available subclasses are:</p>
 * 
 * <ul>
 * <li>
 * {@link int_.esa.eo.ngeo.downloadmanager.exception.AuthenticationException} -
 * Unable to authenticate with UM-SSO</li>
 * <li>
 * {@link int_.esa.eo.ngeo.downloadmanager.exception.FileSystemWriteException} -
 * e.g. disk full, a write lock on file to be written to.</li>
 * <li>
 * {@link int_.esa.eo.ngeo.downloadmanager.exception.ProductUnavailableException}
 * - The plugin has been unable to download the product after a number of
 * retries.</li>
 * <li>
 * {@link int_.esa.eo.ngeo.downloadmanager.exception.UnexpectedResponseException}
 * - for example an HTTP response code which is not expected from a Product
 * Facility.</li>
 * </ul>
 */
public class DMPluginException extends Exception {

    private static final long serialVersionUID = 7917867989020486916L;

    /**
     * Constructs an DMPluginException with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public DMPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an DMPluginException with the specified cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public DMPluginException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an DMPluginException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public DMPluginException(String message) {
        super(message);
    }

}
