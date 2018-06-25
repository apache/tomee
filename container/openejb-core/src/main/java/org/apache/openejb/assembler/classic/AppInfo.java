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

package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AppInfo extends InfoObject {
    public final Properties properties = new Properties();

    public String appId;
    public String path;
    public Set<String> paths = new LinkedHashSet<String>();
    public boolean autoDeploy = true;
    public boolean delegateFirst = true;
    public boolean standaloneModule;
    public final List<ClientInfo> clients = new ArrayList<ClientInfo>();
    public final List<EjbJarInfo> ejbJars = new ArrayList<EjbJarInfo>();
    public final List<IdPropertiesInfo> pojoConfigurations = new ArrayList<IdPropertiesInfo>();
    public final List<ConnectorInfo> connectors = new ArrayList<ConnectorInfo>();
    public final List<WebAppInfo> webApps = new ArrayList<WebAppInfo>();
    public final List<PersistenceUnitInfo> persistenceUnits = new ArrayList<PersistenceUnitInfo>();
    public List<ServiceInfo> services = new ArrayList<ServiceInfo>();
    public List<ContainerInfo> containers = new ArrayList<ContainerInfo>();
    public final List<String> libs = new ArrayList<String>();
    public final Set<String> watchedResources = new TreeSet<String>();
    public final Set<String> resourceIds = new TreeSet<String>();
    public final Set<String> resourceAliases = new TreeSet<String>();
    public final JndiEncInfo globalJndiEnc = new JndiEncInfo();
    public final JndiEncInfo appJndiEnc = new JndiEncInfo();
    public String cmpMappingsXml;
    public final Properties jmx = new Properties();
    public final Set<String> mbeans = new TreeSet<String>();
    public final Set<String> jaxRsProviders = new TreeSet<String>();
    public final Set<String> jsfClasses = new TreeSet<String>();
    public final Set<String> eventClassesNeedingAppClassloader = new TreeSet<String>();
    public boolean webAppAlone;
}
