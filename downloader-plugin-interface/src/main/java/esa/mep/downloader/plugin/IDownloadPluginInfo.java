package esa.mep.downloader.plugin;

/**
 * The purpose of this interface is to provide a means of providing information
 * about a plugin.
 */
public interface IDownloadPluginInfo {

    /**
     * Get the name of the plugin.
     *
     * @return the name of the plugin
     */
    String getName();

    /**
     * Get the version of the plugin. The version is an array of 3 integers,
     * x.y.z.
     *
     * @return the version of the plugin.
     */
    int[] getPluginVersion();

    /**
     * Get the URI patterns which this plugin can use to download a product.
     * Returns one or more patterns used to check if the plugin can process a
     * specific product URI (eg: "S2://.*").
     *
     * @return an array of URI patterns as regular expressions.
     */
    String[] getMatchingPatterns();

    /**
     * The minimum compatible version of the Download Manager. The version is an
     * array of 3 integers, x.y.z.
     *
     * @return the minimum compatible version of the Download Manager.
     */
    int[] getDMMinVersion();

    /**
     * Indicates whether the plugin can handle the pausing of a download.
     *
     * @return true if the download processes created by this plugin can be
     * paused/resumed, false otherwise.
     */
    boolean handlePause();

    /**
     * Get the configuration information of the plugin
     *
     * @param servers The configuration of these particular servers is returned.
     * If none specified, the configuration for all servers of the specified
     * protocol is returned.
     *
     * @return The plugin configurations
     */
    PluginConfiguration[] getConfigurations(String[] servers);

    /**
     * Get protocol is supported by the plugin
     *
     * @return the supported protocol
     */
    PluginConfiguration.Protocol getProtocol();
}
