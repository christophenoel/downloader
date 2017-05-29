package esa.mep.downloader.plugin;

import esa.mep.downloader.exception.DMPluginException;

/**
 * The purpose of this listener interface is provide a means for the plugins to
 * report the status of each download back to the main core of the Download
 * Manager. This is then reported to the user on request via the Web UI and CLI
 * interfaces.
 */
public interface IProductDownloadListener {

    /**
     * Provides information about the product details. This method should be
     * called once the details about the product have been successfully
     * retrieved.
     *
     * @param productName A user-friendly name for the user to identify the
     * product by. This will be based on the file download path (see section
     * 4.2.13 of DAGICD for further information).
     * @param numberOfFiles The number of files that are contained in this
     * product. A number greater than 1 indicates the initial product URL points
     * to a metalink file.
     * @param overallSize The overall file size of the file(s) in bytes. -1
     * indicates an unknown size, which may happen if the product is transferred
     * using chunked encoding.
     */
    void productDetails(String productName, Integer numberOfFiles,
            Long overallSize);

    /**
     * Provides information about the progress of a product download.
     *
     * @param progressPercentage The progress of the product download, typically
     * between 0 and 100. -1 indicates an unknown progress, which may happen if
     * the product is transferred using chunked encoding.
     * @param downloadedSize The number of bytes that have been downloaded for
     * this product so far.
     * @param status The current status of the download. The status is of type
     * {@link esa.mep.downloader.plugin.EDownloadStatus}.
     * @param message A message to display to the user. Typically this is used
     * to provide further details for error cases.
     */
    void progress(Integer progressPercentage, Long downloadedSize,
            EDownloadStatus status, String message);

    /**
     * Provides information about the progress of a product download.
     *
     * @param progressPercentage The progress of the product download, typically
     * between 0 and 100. -1 indicates an unknown progress, which may happen if
     * the product is transferred using chunked encoding.
     * @param downloadedSize The number of bytes that have been downloaded for
     * this product so far.
     * @param status The current status of the download. The status is of type
     * {@link esa.mep.downloader.plugin.EDownloadStatus}.
     * @param message A message to display to the user. Typically this is used
     * to provide further details for error cases.
     * @param exception An optional parameter which provides the exception
     * thrown by the plugin if an error has ocurred. This allows the Download
     * Manager to provide the user with further details of the error and allows
     * granular configuration of email notifications based on the error type.
     * <p>
     * The exception is of type
     * {@link esa.mep.downloader.exception.DMPluginException} and can be one of
     * the following sub types to provide more detail:</p>
     * <ul>
     * <li>{@link esa.mep.downloader.exception.AuthenticationException} - Unable
     * to authenticate with UM-SSO</li>
     * <li>{@link esa.mep.downloader.exception.FileSystemWriteException} - e.g.
     * disk full, a write lock on file to be written to.</li>
     * <li>{@link esa.mep.downloader.exception.ProductUnavailableException} -
     * The plugin has been unable to download the product after a number of
     * retries.</li>
     * <li>{@link esa.mep.downloader.exception.UnexpectedResponseException} -
     * for example an HTTP response code which is not expected from a Product
     * Facility.</li>
     * </ul>
     */
    void progress(Integer progressPercentage, Long downloadedSize,
            EDownloadStatus status, String message, DMPluginException exception);
}
