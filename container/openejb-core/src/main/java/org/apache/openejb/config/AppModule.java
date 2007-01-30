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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class AppModule implements DeploymentModule {
    private final List<URL> additionalLibraries = new ArrayList<URL>();
    private final List<ClientModule> clientModules = new ArrayList<ClientModule>();
    private final List<EjbModule> ejbModules = new ArrayList<EjbModule>();
    private final List<PersistenceModule> persistenceModules = new ArrayList<PersistenceModule>();;
    private final String jarLocation;
    private final ClassLoader classLoader;
    public String cmpMappingsXml;

    public AppModule(ClassLoader classLoader, String jarLocation) {
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<ClientModule> getClientModules() {
        return clientModules;
    }

    public List<EjbModule> getEjbModules() {
        return ejbModules;
    }

    public List<PersistenceModule> getPersistenceModules() {
        return persistenceModules;
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public List<URL> getAdditionalLibraries() {
        return additionalLibraries;
    }

    public String getCmpMappingsXml() {
        return cmpMappingsXml;
    }

    public void setCmpMappingsXml(String cmpMappingsXml) {
        this.cmpMappingsXml = cmpMappingsXml;
    }
}
