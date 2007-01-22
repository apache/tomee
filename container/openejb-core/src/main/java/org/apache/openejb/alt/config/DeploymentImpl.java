/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.config;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentImpl implements Deployment {
    private final URI uri;
    private final String user;
    private final String password;
    private final Properties properties = new Properties();

    public DeploymentImpl(URI uri, String user, String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public URI getUri() {
        return uri;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void release() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Properties getProperties() {
        return properties;
    }

    public Set<String> list(String type, String state, Set<String> targets) throws DeploymentException {
        return Collections.emptySet();
    }

    public Set<String> deploy(Set<String> targets, File file) throws DeploymentException {
        return Collections.emptySet();
    }

    public Set<String> start(Set<String> modules) throws DeploymentException {
        return modules;
    }

    public Set<String> stop(Set<String> modules) throws DeploymentException {
        return modules;
    }

    public Set<String> restart(Set<String> modules) throws DeploymentException {
        return modules;
    }

    public Set<String> undeploy(Set<String> modules) throws DeploymentException {
        return modules;
    }

    public static class DeploymentFactoryImpl implements DeploymentFactory {
        public Deployment createDeployment(URI uri, String user, String password) {
            return new DeploymentImpl(uri, user, password);
        }
    }
}
