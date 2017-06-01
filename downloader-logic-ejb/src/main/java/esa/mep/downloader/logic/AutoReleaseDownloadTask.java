/*
 * Copyright 2017 mng.
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
package esa.mep.downloader.logic;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.ProductType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mng
 */
public class AutoReleaseDownloadTask implements Runnable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AutoReleaseDownloadTask.class);
    private final DownloadManager manager;
    private final DownloadTask task;
    private final String baseDownloadDir;

    public AutoReleaseDownloadTask(DownloadManager manager, DownloadTask newTask, String newBaseDownloadDir) {
        this.manager = manager;
        this.task = newTask;
        this.baseDownloadDir = newBaseDownloadDir;
    }

    @Override
    public void run() {
        task.cancel();

        if (baseDownloadDir != null && !baseDownloadDir.isEmpty()) {
            // wait a moment
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }

            // delete downloaded directories
            DownloadRequest request = task.getRequest();
            if (request.getProducts() != null) {
                for (ProductType pType : request.getProducts()) {
                    try {
                        File dwlDir = Paths.get(baseDownloadDir, pType.getDownloadDirectory()).toFile();
                        if (dwlDir.exists() && dwlDir.isDirectory()) {
                            FileUtils.deleteDirectory(dwlDir);
                        }
                    } catch (IOException e) {
                        LOGGER.debug(e.getMessage());
                    }

                }
            }
        }
        // remove the task from task list
        manager.removeTask(task.getIdentifier());
    }

}
