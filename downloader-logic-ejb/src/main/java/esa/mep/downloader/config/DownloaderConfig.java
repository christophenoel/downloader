/*
 * Copyright 2017 cnl.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package esa.mep.downloader.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
/**
 * Retrieve all configuration information variable
 *
 * @author cnl
 */
public class DownloaderConfig {

    private static final String CONF_DIR = "conf";
    private static final String PLUGIN_DIR = "plugins";
    private static final String BASE_DOWNLOAD_DIR_ABSOLUTE = "base.download.directory.absolute";
    private static final String TASK_DURATION = "download.task.maximum.duration.hours";
    private static final String TASK_EXPIRATION = "download.task.expiration_miniutes";

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderConfig.class);

    private final Properties prop = new Properties();
    private String downloaderRootDirectory;
    private String baseDownloadDir;
    private int taskDuration = 72;
    private int taskExpiration = 5;

    @PostConstruct
    void init() {
        LOGGER.debug("Initialize DownloaderConfig class");
        File classPath = new File(DownloaderConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        LOGGER.debug("DownloaderConfig class location = " + classPath);

        this.downloaderRootDirectory = classPath.getParentFile().getParentFile().getParentFile().getAbsolutePath();
        LOGGER.debug("Downloader root directory = " + downloaderRootDirectory);

        try {
            this.prop.load(new FileInputStream(Paths.get(downloaderRootDirectory, CONF_DIR, "resources.properties").toFile()));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        this.baseDownloadDir = getProp(BASE_DOWNLOAD_DIR_ABSOLUTE);

        String strDuration = getProp(TASK_DURATION);
        if (StringUtils.isNotEmpty(strDuration)) {
            try {
                taskDuration = Integer.parseInt(strDuration);
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("The value of %s should be a positive number.", TASK_DURATION));
            }
        }
        LOGGER.debug("The configrured duration of download task is " + taskDuration + " hours");

        String strExpiration = getProp(TASK_EXPIRATION);
        if (StringUtils.isNotEmpty(strExpiration)) {
            try {
                this.taskExpiration = Integer.parseInt(strExpiration);
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("The value of %s should be a positive number.", TASK_EXPIRATION));
            }
        }
        LOGGER.debug("The configrured expiration of download task is " + this.taskExpiration + " minutes");
    }

    public Path getPluginConfigurationDirectory() {
        Path path = Paths.get(downloaderRootDirectory, CONF_DIR, PLUGIN_DIR);
        LOGGER.debug("Plugins configuration dir = " + path.toString());
        return path;
    }    

    public String getBaseDownloadDirectory() {
        return baseDownloadDir;
    }

    public int getTaskDuration() {
        return taskDuration;
    }

    public int getTaskExpiration() {
        return taskExpiration;
    }

    private String getProp(String key) {
        String value = prop.getProperty(key);
        if (StringUtils.isNotEmpty(value)) {
            return StringUtils.trimToEmpty(value);
        } else {
            LOGGER.error(String.format("The property %s does not exist or is empty.", key));
            return null;
        }
    }

}
