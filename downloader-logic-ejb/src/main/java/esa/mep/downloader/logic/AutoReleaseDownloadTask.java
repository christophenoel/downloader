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

/**
 *
 * @author mng
 */
public class AutoReleaseDownloadTask implements Runnable {

    private final DownloadManager manager;
    private final String taskId;

    public AutoReleaseDownloadTask(DownloadManager manager, String newTaskId) {
        this.manager = manager;
        this.taskId = newTaskId;
    }

    @Override
    public void run() {
        manager.removeTask(taskId);
    }

}
