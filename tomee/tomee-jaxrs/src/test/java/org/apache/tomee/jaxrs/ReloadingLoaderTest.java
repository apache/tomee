/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.jaxrs;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.IvmJndiFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.cxf.rs.CxfRSService;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomee.catalina.TomEEWebappClassLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ReloadingLoaderTest {
    private AppInfo info;
    private AppContext context;
    private TomEEWebappClassLoader loader;
    private AtomicReference<ClassLoader> parentInstance;

    @BeforeClass
    @AfterClass
    public static void resetSystemInstance() {
        SystemInstance.reset();
    }

    @Before
    public void initContext() throws LifecycleException {
        final OpenEjbConfiguration configuration = new OpenEjbConfiguration();
        configuration.facilities = new FacilitiesInfo();

        final CoreContainerSystem containerSystem = new CoreContainerSystem(new IvmJndiFactory());

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, configuration);
        SystemInstance.get().setComponent(ContainerSystem.class, containerSystem);
        SystemInstance.get().setComponent(WebAppEnricher.class, new WebAppEnricher() {
            @Override
            public URL[] enrichment(final ClassLoader webappClassLaoder) {
                return new URL[0];
            }
        });

        parentInstance = new AtomicReference<>(ParentClassLoaderFinder.Helper.get());
        loader = new TomEEWebappClassLoader(parentInstance.get()) {
            @Override
            public ClassLoader getInternalParent() {
                return parentInstance.get();
            }

            @Override
            protected void clearReferences() {
                // no-op: this test should be reworked to support it but in real life a loader is not stopped/started
            }
        };
        loader.init();
        final StandardRoot resources = new StandardRoot();
        loader.setResources(resources);
        resources.setContext(new StandardContext() {
            @Override
            public String getDocBase() {
                final File file = new File("target/foo");
                file.mkdirs();
                return file.getAbsolutePath();
            }

            @Override
            public String getMBeanKeyProperties() {
                return "foo";
            }
        {}});
        resources.start();
        loader.start();

        info = new AppInfo();
        info.appId = "test";
        context = new AppContext(info.appId, SystemInstance.get(), loader, new IvmContext(), new IvmContext(), true);
        containerSystem.addAppContext(context);

        final WebContext webDeployment = new WebContext(context);
        webDeployment.setId(context.getId());
        webDeployment.setClassLoader(loader);
        containerSystem.addWebContext(webDeployment);
    }

    @After
    public void cleanUp() throws LifecycleException {
        loader.stop();
    }

    @Test
    public void tomcatClassLoaderParentShouldntBeNulAfterAStopStartOtherwiseReloadIsBroken() throws Exception {
        final CxfRSService server = new CxfRSService();
        try {
            server.init(new Properties());
            server.start();

            server.afterApplicationCreated(new AssemblerAfterApplicationCreated(info, context, Collections.<BeanContext>emptyList()));

            {
                final ClassLoader beforeLoader = SystemInstance.get().getComponent(ContainerSystem.class).getWebContext("test").getClassLoader();
                assertSame(loader, beforeLoader);
                assertNotNull(beforeLoader);
                assertNotNull(Reflections.get(beforeLoader, "parent"));
            }

            loader.internalStop();

            server.undeploy(new AssemblerBeforeApplicationDestroyed(info, context));

            {
                final URLClassLoader afterLoader = URLClassLoader.class.cast(SystemInstance.get().getComponent(ContainerSystem.class).getWebContext("test").getClassLoader());
                assertSame(loader, afterLoader);
                assertNotNull(afterLoader);
                assertEquals(0, afterLoader.getURLs().length);
                assertEquals(LifecycleState.STOPPED, loader.getState());
            }

            final StandardRoot resources = new StandardRoot();
            loader.setResources(resources);
            resources.setContext(new StandardContext() {
                @Override
                public String getDocBase() {
                    final File file = new File("target/foo");
                    file.mkdirs();
                    return file.getAbsolutePath();
                }

                @Override
                public String getMBeanKeyProperties() {
                    return "foo";
                }
                {}});
            resources.start();
            loader.start();
            // TomcatWebAppBuilder ill catch start event from StandardContext and force a classloader
            // Reflections.set(loader, "parent", ParentClassLoaderFinder.Helper.get());
            parentInstance.set(ParentClassLoaderFinder.Helper.get());

            server.afterApplicationCreated(new AssemblerAfterApplicationCreated(info, context, Collections.<BeanContext>emptyList()));

            {
                final ClassLoader afterLoader = SystemInstance.get().getComponent(ContainerSystem.class).getWebContext("test").getClassLoader();
                assertSame(loader, afterLoader);
                assertNotNull(afterLoader);
                assertNotNull(Reflections.get(afterLoader, "parent"));
            }

            server.undeploy(new AssemblerBeforeApplicationDestroyed(info, context));
        } finally {
            server.stop();
        }
    }
}
