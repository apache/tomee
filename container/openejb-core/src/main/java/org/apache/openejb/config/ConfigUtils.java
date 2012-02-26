/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.EnvProps;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ConfigUtils {

    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    public static String searchForConfiguration() throws OpenEJBException {
        return searchForConfiguration(SystemInstance.get().getOptions().get("openejb.configuration", (String) null));
    }

    public static String searchForConfiguration(String path) throws OpenEJBException {
        return ConfigUtils.searchForConfiguration(path, SystemInstance.get().getProperties());
    }

    /**
     * TODO: It should always be assumed that the path input param is a URL or URL-convertible
     * 
     * @param path
     * @param props
     * @return
     * @throws OpenEJBException
     */
    public static String searchForConfiguration(String path, Properties props) throws OpenEJBException {
        File file = null;
        if (path != null) {
            /*
             * [1] Try finding the file relative to the current working
             * directory
             */
            file = new File(path);
            if (file != null && file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }

            /*
             * [2] Try finding the file relative to the openejb.base directory
             */
            try {
                file = SystemInstance.get().getBase().getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }

            /*
             * [3] Try finding the file relative to the openejb.home directory
             */
            try {
                file = SystemInstance.get().getHome().getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }

            /*
             * [4] Consider path as a URL resource - file: and jar: accepted by JaxbOpenejb.readConfig(String configFile)
             */
            try {
                // verify if it's parseable according to URL rules
                new URL(path);
                // it's so return it unchanged
                return path;
            } catch (MalformedURLException ignored) {
            }
            
            logger.warning("Cannot find the configuration file [" + path + "], Trying conf/openejb.xml instead.");
        }


        try {
            /*
             * [4] Try finding the standard openejb.xml file relative to the
             * openejb.base directory
             */
            try {
                file = SystemInstance.get().getBase().getFile("conf/openejb.xml");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e) {
            }

            /*
             * [5] Try finding the standard openejb.conf file relative to the
             */
            try {
                file = SystemInstance.get().getBase().getFile("conf/openejb.conf");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e) {
            }


            if (EnvProps.extractConfigurationFiles()) {

                /* [6] No config found! Create a config for them
                 *     using the default.openejb.conf file from
                 *     the openejb-x.x.x.jar
                 */

                final File confDir = SystemInstance.get().getBase().getDirectory("conf", false);

                if (confDir.exists()) {
                    File config = new File(confDir, "openejb.xml");
                    logger.info("Cannot find the configuration file [conf/openejb.xml].  Creating one at "+config.getAbsolutePath());
                    file = createConfig(config);
                } else {
                    logger.info("Cannot find the configuration file [conf/openejb.xml].  Will attempt to create one for the beans deployed.");
                }
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new OpenEJBException("Could not locate config file: ", e);
        }

        return (file == null) ? null : file.getAbsolutePath();
    }

    public static File createConfig(File config) throws java.io.IOException {
        ResourceFinder finder = new ResourceFinder("");
        URL defaultConfig = finder.find("default.openejb.conf");

        IO.copy(IO.read(defaultConfig), config);

        return config;
    }

    public static boolean addDeploymentEntryToConfig(String jarLocation, Openejb config) {
        File jar = new File(jarLocation);

        /* Check to see if the entry is already listed */
        for (Deployments d : config.getDeployments()) {

            if (d.getJar() != null) {
                try {
                    File target = SystemInstance.get().getBase().getFile(d.getJar(), false);

                    /* 
                     * If the jar entry is already there, no need 
                     * to add it to the config or go any futher.
                     */
                    if (jar.equals(target)) return false;
                } catch (java.io.IOException e) {
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            } else if (d.getDir() != null) {
                try {
                    File target = SystemInstance.get().getBase().getFile(d.getDir(), false);
                    File jarDir = jar.getAbsoluteFile().getParentFile();

                    /* 
                     * If a dir entry is already there, the jar
                     * will be loaded automatically.  No need 
                     * to add it explicitly to the config or go
                     * any futher.
                     */
                    if (jarDir != null && jarDir.equals(target)) return false;
                } catch (java.io.IOException e) {
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            }
        }

        /* Create a new Deployments entry */
        Deployments dep = JaxbOpenejb.createDeployments();
        dep.setJar(jarLocation);
        config.getDeployments().add(dep);
        return true;
    }
}
