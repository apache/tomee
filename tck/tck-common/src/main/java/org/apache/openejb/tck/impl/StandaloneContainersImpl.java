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
package org.apache.openejb.tck.impl;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.jboss.testharness.api.DeploymentException;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneContainersImpl
    implements org.jboss.testharness.spi.StandaloneContainers
{

    private final ContainersImpl containers = new ContainersImpl();

    protected ContainersImpl getContainers() {
        return containers;
    }

    @Override
    public void deploy(Collection<Class<?>> classes)
        throws DeploymentException
    {
        deploy(classes, Collections.EMPTY_LIST);
    }

    @Override
    public boolean deploy(Collection<Class<?>> classes, Collection<URL> xmls)
    {
        final Archive archive = new Archive(xmls, classes);
        return containers.deploy(archive.getIn(), archive.getName());
    }

    @Override
    public DeploymentException getDeploymentException()
    {
        return containers.getDeploymentException();
    }

    @Override
    public void undeploy()
    {
        containers.undeploy(null);
    }

    @Override
    public void setup()
    {
    }

    @Override
    public void cleanup()
    {
    }
}
