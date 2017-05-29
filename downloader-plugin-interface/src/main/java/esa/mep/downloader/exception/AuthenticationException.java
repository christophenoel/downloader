package esa.mep.downloader.exception;

/**
* <p>
* This exception is used to indicate an error when attempting to authenticate 
* with the Download Manager for the download of a product. Typically this 
* authentication is provided by UM-SSO.
* </p>
*/
public class AuthenticationException extends DMPluginException {
    private static final long serialVersionUID = 6825781677077686191L;

    /**
     * Constructs an AuthenticationException with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an AuthenticationException with the specified cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an AuthenticationException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public AuthenticationException(String message) {
        super(message);
    }

}
