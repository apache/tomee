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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.config.RequireDescriptors;
import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.xbean.naming.context.ContextFlyweight;

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
            Object provider = properties.get(EJBContainer.PROVIDER);
            if (provider != null && !provider.equals(OpenEjbContainer.class) && !provider.equals(OpenEjbContainer.class.getName())) {
                return null;
            }
            String appId = (String) properties.get(EJBContainer.APP_NAME);
            try {
                Properties props = new Properties();
                props.put(DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY, Boolean.toString(false));
                //This causes scan of the entire classpath except for default excludes.  This may be quite slow.
                props.put(DeploymentsResolver.CLASSPATH_INCLUDE, ".*");
                props.putAll(properties);
                OpenEJB.init(props);
                ConfigurationFactory configurationFactory = new ConfigurationFactory();
                List<File> moduleLocations;
                ClassLoader classLoader = getClass().getClassLoader();
                Object modules = properties.get(EJBContainer.MODULES);
                if (modules instanceof String) {
                    moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                    for (Iterator<File> i = moduleLocations.iterator(); i.hasNext(); ) {
                        File file = i.next();
                        if (!match((String)modules, file)) {
                            i.remove();
                        }
                    }
                } else if (modules instanceof String[]) {
                    moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                    int matched = 0;
                    for (Iterator<File> i = moduleLocations.iterator(); i.hasNext(); ) {
                        File file = i.next();
                        boolean remove = true;
                        for (String s: (String[])modules) {
                            if (match(s, file)) {
                                remove = false;
                                matched++;
                                break;
                            }
                        }
                        if (remove) {
                            i.remove();
                        }
                    }
                    if (matched != ((String[])modules).length) {
                        throw new IllegalStateException("some modules not matched on classpath");
                    }

                } else if (modules instanceof File) {
                    URL url = ((File) modules).toURI().toURL();
                    classLoader = new URLClassLoader(new URL[] {url}, classLoader);
                    moduleLocations = Collections.singletonList((File)modules);
                } else if (modules instanceof File[]) {
                    File[] files = (File[]) modules;
                    URL[] urls = new URL[files.length];
                    for (int i = 0; i< urls.length; i++) {
                        urls[i] = files[i].toURI().toURL();
                    }
                    classLoader = new URLClassLoader(urls, classLoader);
                    moduleLocations = Arrays.asList((File[])modules);
                } else if (modules == null) {
                    moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                } else {
                    throw new IllegalStateException("Unrecognized modules property");
                }
                if (moduleLocations.isEmpty()) {
                    throw new IllegalStateException("No modules to deploy found");
                }
                AppInfo appInfo = configurationFactory.configureApplication(classLoader, appId, moduleLocations);

                Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                try {
                    assembler.createApplication(appInfo, classLoader);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not deploy embedded app", e);
                }
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
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }

        private boolean match(String s, File file) {
            String s2 = file.getName();
            boolean matches;
            if (file.isDirectory()) {
                matches = s2.equals(s) || s2.equals(s + ".jar");
            } else {
                matches = s2.equals(s + ".jar");
            }
            if (!matches) {
                //TODO look for ejb-jar.xml with matching module name
            }
            return matches;
        }
    }
}
