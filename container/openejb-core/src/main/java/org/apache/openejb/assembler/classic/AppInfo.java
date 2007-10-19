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
package org.apache.openejb.assembler.classic;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AppInfo extends InfoObject {
    public String jarPath;
    public final List<ClientInfo> clients = new ArrayList<ClientInfo>();
    public final List<EjbJarInfo> ejbJars = new ArrayList<EjbJarInfo>();
    public final List<ConnectorInfo> connectors = new ArrayList<ConnectorInfo>();
    public final List<WebAppInfo> webApps = new ArrayList<WebAppInfo>();
    public final List<PersistenceUnitInfo> persistenceUnits = new ArrayList<PersistenceUnitInfo>();
    public final List<String> libs = new ArrayList<String>();
    public final Set<String> watchedResources = new TreeSet<String>();
    public String cmpMappingsXml;
}
