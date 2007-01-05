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
package org.apache.openejb.assembler.dynamic;

import org.apache.openejb.assembler.classic.InfoObject;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.alt.config.DeploymentModule;
import org.apache.openejb.alt.config.AppModule;
import org.apache.openejb.alt.config.EjbModule;
import org.apache.openejb.alt.config.ClientModule;
import org.apache.openejb.OpenEJBException;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class ModuleDeployer {

    public InfoObject deploy(DeploymentModule url) throws OpenEJBException {
        return null;
    }

    public AppInfo deployApp(AppModule ejbModule) throws OpenEJBException {
        return null;
    }

    public EjbJarInfo deployEjbJar(EjbModule ejbModule) throws OpenEJBException {
        return null;
    }

    public ClientInfo deployClient(ClientModule clientModule) throws OpenEJBException {
        return null;
    }

    public InfoObject deploy(URL url) throws OpenEJBException {
        return null;
    }

    public AppInfo deployApp(URL url) throws OpenEJBException {
        return null;
    }

    public EjbJarInfo deployEjbJar(URL url) throws OpenEJBException {
        return null;
    }

    public ClientInfo deployClient(URL url) throws OpenEJBException {
        return null;
    }
}
