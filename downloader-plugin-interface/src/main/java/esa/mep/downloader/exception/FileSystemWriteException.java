package esa.mep.downloader.exception;

/**
* <p>
* This exception is used to indicate an error when attempting to write a product 
* download.</p>
* 
* <p>Examples of when this will be thrown include:</p>
* <ul>
*   <li>Unable to access the download location specified in the Download
*   Manager configuration.</li>
*   <li>The disk which is being written is full</li>
*   <li>The plugin does not have permission to write to the file (either user or 
*   system process permissions).</li>
* </ul>
*/
public class FileSystemWriteException extends DMPluginException {
    private static final long serialVersionUID = -1421899484497584027L;

    /**
     * Constructs an FileSystemWriteException with the specified detail message and
     * cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public FileSystemWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an FileSystemWriteException with the specified cause.
     * 
     * @param cause
     *            the cause of the exception
     */
    public FileSystemWriteException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an FileSystemWriteException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public FileSystemWriteException(String message) {
        super(message);
    }

}
