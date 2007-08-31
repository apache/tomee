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
package org.apache.openejb.config;

import org.apache.openejb.jee.ApplicationClient;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class ClientModule implements DeploymentModule {
    private final ValidationContext validation;
    private ApplicationClient applicationClient;
    private String jarLocation;
    private ClassLoader classLoader;
    private String mainClass;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();
    private final String moduleId;

    public ClientModule(ApplicationClient applicationClient, ClassLoader classLoader, String jarLocation, String mainClass, String moduleId) {
        this.applicationClient = applicationClient;
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;
        this.mainClass = mainClass;

        if (moduleId == null){
            if (applicationClient != null && applicationClient.getId() != null){
                moduleId = applicationClient.getId();
            } else {
                File file = new File(jarLocation);
                moduleId = file.getName();
            }
        }

        this.moduleId = moduleId;
        validation = new ValidationContext(ClientModule.class, jarLocation);
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public ApplicationClient getApplicationClient() {
        return applicationClient;
    }

    public void setApplicationClient(ApplicationClient applicationClient) {
        this.applicationClient = applicationClient;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public void setJarLocation(String jarLocation) {
        this.jarLocation = jarLocation;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
}
