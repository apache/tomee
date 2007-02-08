/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.ejb.Stateless;
import javax.ejb.Remote;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.loader.SystemInstance;

@Stateless(name = "openejb/Deployer")
@Remote(Deployer.class)
public class DeployerEjb implements Deployer {
    private final static File uniqueFile;

    static {
        String uniqueName = "OpenEJB-" + new BigInteger(128, new SecureRandom()).toString(Character.MAX_RADIX);
        String tempDir = System.getProperty("java.io.tmpdir");
        uniqueFile = new File(tempDir, uniqueName);
        try {
            uniqueFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        uniqueFile.deleteOnExit();
    }

    private final DeploymentLoader deploymentLoader;
    private final ConfigurationFactory configurationFactory;
    private final Assembler assembler;

    public DeployerEjb() {
        deploymentLoader = new DeploymentLoader();
        configurationFactory = new ConfigurationFactory();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
    }

    public String getUniqueFile() {
        return uniqueFile.getAbsolutePath();
    }

    public Collection<AppInfo> getDeployedApps() {
        return assembler.getDeployedApplications();
    }

    public AppInfo deploy(String location) throws OpenEJBException {
        return deploy(location, null);
    }

    public AppInfo deploy(Properties properties) throws OpenEJBException {
        return deploy(null, properties);
    }

    public AppInfo deploy(String location, Properties properties) throws OpenEJBException {
        if (location == null && properties == null) {
            throw new NullPointerException("location and properties are null");
        }
        if (location == null) {
            location = properties.getProperty(FILENAME);
        }
        if (properties == null) {
            properties = new Properties();
        }

        try {
            File file = new File(location);
            AppModule appModule = deploymentLoader.load(file);

            // Add any alternate deployment descriptors to the modules
            Map<String, DeploymentModule> modules = new TreeMap<String, DeploymentModule>();
            for (DeploymentModule module : appModule.getEjbModules()) {
                modules.put(module.getModuleId(), module);
            }
            for (DeploymentModule module : appModule.getClientModules()) {
                modules.put(module.getModuleId(), module);
            }
            for (DeploymentModule module : appModule.getWebModules()) {
                modules.put(module.getModuleId(), module);
            }
            for (DeploymentModule module : appModule.getResourceModules()) {
                modules.put(module.getModuleId(), module);
            }

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                if (name.startsWith(ALT_DD + "/")) {
                    name = name.substring(ALT_DD.length() + 1);

                    DeploymentModule module;
                    int slash = name.indexOf('/');
                    if (slash > 0) {
                        String moduleId = name.substring(0, slash);
                        name = name.substring(slash + 1);
                        module = modules.get(moduleId);
                    } else {
                        module = appModule;
                    }

                    if (module != null) {
                        String value = (String) entry.getValue();
                        File dd = new File(value);
                        if (dd.canRead()) {
                            module.getAltDDs().put(name, dd.toURL());
                        } else {
                            module.getAltDDs().put(name, value);
                        }
                    }
                }
            }

            AppInfo appInfo = configurationFactory.configureApplication(appModule);
            assembler.createApplication(appInfo);

            return appInfo;
        } catch (OpenEJBException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenEJBException(e);
        }
    }

    public void undeploy(String moduleId) throws UndeployException, NoSuchApplicationException {
        assembler.destroyApplication(moduleId);
    }
}
