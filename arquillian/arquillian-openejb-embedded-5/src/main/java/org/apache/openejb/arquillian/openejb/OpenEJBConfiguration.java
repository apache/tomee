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
package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.config.descriptor.api.Multiline;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;

public class OpenEJBConfiguration implements ContainerConfiguration {
    private String properties = "";
    private String preloadClasses;
    private boolean startDefaultScopes;
    private Collection<String> singleDeploymentByArchiveName = Collections.emptyList();

    @Override
    public void validate() throws ConfigurationException {
        // no-op
    }

    public boolean isStartDefaultScopes() {
        return startDefaultScopes;
    }

    public void setStartDefaultScopes(final boolean startDefaultScopes) {
        this.startDefaultScopes = startDefaultScopes;
    }

    public String getProperties() {
        return properties;
    }

    @Multiline
    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public String getPreloadClasses() {
        return preloadClasses;
    }

    public void setPreloadClasses(final String preloadClasses) {
        this.preloadClasses = preloadClasses;
    }

    public boolean isSingleDeploymentByArchiveName(final String name) {
        return singleDeploymentByArchiveName.contains(name) || singleDeploymentByArchiveName.contains("*") || singleDeploymentByArchiveName.contains("true");
    }

    public void setSingleDeploymentByArchiveName(final String singleDeploymentByArchiveName) {
        this.singleDeploymentByArchiveName = singleDeploymentByArchiveName == null || singleDeploymentByArchiveName.trim().isEmpty() ?
                Collections.<String>emptyList() : new HashSet<String>(asList(singleDeploymentByArchiveName.split(" *, *")));
    }
}
