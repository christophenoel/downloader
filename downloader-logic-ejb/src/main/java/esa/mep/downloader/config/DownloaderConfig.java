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


import javax.annotation.Resource;
import javax.ejb.Singleton;

@Singleton
/**
 * Retrieve all configuration information variable
 * @author cnl
 */
public class DownloaderConfig {

    @Resource(name = "downloaderRootDirectory")
    private String downloaderRootDirectory;
    
    public String getPluginConfigurationDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getDownloaderRootDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
