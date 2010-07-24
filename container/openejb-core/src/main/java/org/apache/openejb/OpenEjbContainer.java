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
package org.apache.openejb;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.client.LocalInitialContext;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ModuleContext;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.xbean.naming.context.ContextFlyweight;

import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbContainer extends EJBContainer {

    private final Context context;

    private OpenEjbContainer(Context context) {
        this.context = context;
    }

    @Override
    public void close() {
        try {
            context.close();
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
        OpenEJB.destroy();
    }

    @Override
    public Context getContext() {
        return context;
    }


    public static class Provider implements EJBContainerProvider {

        @Override
        public EJBContainer createEJBContainer(Map<?, ?> properties) {
            try {
                Properties props = new Properties();
                props.putAll(properties);
                OpenEJB.init(props);
                Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                Collection<AppInfo> apps = assembler.getDeployedApplications();
                if (apps.size() != 1) {
                    throw new IllegalStateException("not exactly one app deployed in embedded: " + apps.size());
                }
                ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                AppContext appContext = null;
                DeploymentInfo[] infos = containerSystem.deployments();
                for (DeploymentInfo info: infos) {
                    if (info instanceof CoreDeploymentInfo) {
                        appContext = ((CoreDeploymentInfo)info).getModuleContext().getAppContext();
                        break;
                    }
                }
                if (appContext == null) {
                    throw new IllegalStateException("Could not locate app context");
                }
                final Context globalJndiContext = appContext.getGlobalJndiContext();
                return new OpenEjbContainer(new ContextFlyweight() {

                    @Override
                    protected Context getContext() throws NamingException {
                        return globalJndiContext;
                    }

                    @Override
                    protected Name getName(Name name) throws NamingException {
                        String first = name.get(0);
                        if (!first.startsWith("java:")) throw new NameNotFoundException("Name must be in java: namespace");
                        first = first.substring("java:".length());
                        name = name.getSuffix(1);
                        return name.add(0, first);
                    }

                    @Override
                    protected String getName(String name) throws NamingException {
                        if (!name.startsWith("java:")) throw new NameNotFoundException("Name must be in java: namespace");
                        return name.substring("java:".length());
                    }
                });
            } catch (OpenEJBException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
