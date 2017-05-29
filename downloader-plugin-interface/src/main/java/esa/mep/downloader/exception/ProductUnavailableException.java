package esa.mep.downloader.exception;

/**
* <p>
* This exception is used to indicate an error when attempting to download a
* product after a number of retries. Typically this will be thrown when an 
* HTTP 400 or 404 response code is returned from a request to the product URL.
* </p>
* 
*/
public class ProductUnavailableException extends DMPluginException {
    private static final long serialVersionUID = 3454040234488733223L;

    /**
     * Constructs an ProductUnavailableException with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public ProductUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an ProductUnavailableException with the specified cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public ProductUnavailableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an ProductUnavailableException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public ProductUnavailableException(String message) {
        super(message);
    }

}
