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

import org.apache.openejb.jee.jpa.unit.Persistence;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

public class PersistenceModule implements DeploymentModule {
    private String rootUrl;
    private Persistence persistence;
    private final Set<String> watchedResources = new TreeSet<String>();
    private URI uri;

    public PersistenceModule(String rootUrl, Persistence persistence) {
        setRootUrl(rootUrl);
        this.persistence = persistence;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
        this.uri = URI.create(rootUrl);
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    public Map<String, Object> getAltDDs() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public String getJarLocation() {
        return null;
    }

    public String getModuleId() {
        return null;
    }

    public URI getModuleUri() {
        return uri;
    }

    public File getFile() {
        return null;
    }

    public ValidationContext getValidation() {
        return null;
    }

    @Override
    public String toString() {
        return "PersistenceModule{" +
                "rootUrl='" + rootUrl + '\'' +
                '}';
    }

    public boolean isStandaloneModule() {
        return false;
    }

    public void setStandaloneModule(boolean isStandalone) {
       //do nothing        
    }
}
