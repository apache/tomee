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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MBeanDeployer implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, MBeanDeployer.class);

    // mbeans ObjectNames are stored in the app since they are global and that's easier
    // mbean classes themself are stored in modules since they depend only on them

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        logger.debug("looking for annotated MBeans in " + appModule.getModuleId());
        final List<String> done = new ArrayList<String>();

        ClassLoader cl = appModule.getClassLoader();
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = getClass().getClassLoader();
            }
        }

        final Collection<Class<? extends Annotation>> mbeanClasses = new ArrayList<Class<? extends Annotation>>(2);
        mbeanClasses.add(org.apache.openejb.api.jmx.MBean.class);

        try { // for OSGi environment, javax.management is imported by the JRE
            mbeanClasses.add((Class<? extends Annotation>) cl.loadClass("javax.management.MBean"));
        } catch (NoClassDefFoundError noClassDefFoundError) {
            // ignored
        } catch (ClassNotFoundException e) {
            // ignored
        }

        // there is an ejbmodule by webapp so we should't need to go through the webapp

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            if (ejbModule.getFinder() == null) {
                continue;
            }

            for (Class<? extends Annotation> mclazz : mbeanClasses) {
                for (Annotated<Class<?>> clazz : ejbModule.getFinder().findMetaAnnotatedClasses(mclazz)) {
                    final Class<?> realClass = clazz.get();
                    final String name = realClass.getName();
                    if (done.contains(name)) {
                        continue;
                    }

                    ejbModule.getMbeans().add(name);
                    done.add(name);
                }
            }
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            if (clientModule.getFinder() == null) {
                continue;
            }

            for (Class<? extends Annotation> mclazz : mbeanClasses) {
                for (Annotated<Class<?>> clazz : clientModule.getFinder().findMetaAnnotatedClasses(mclazz)) {
                    final String name = clazz.get().getName();
                    if (done.contains(name)) {
                        continue;
                    }

                    clientModule.getMbeans().add(name);
                }
            }
        }

        List<URL> libs = appModule.getAdditionalLibraries();
        Iterator<URL> it = libs.iterator();
        while (it.hasNext()) {
            URL url = it.next();
            for (String location : done) {
                if (url.getFile().equals(location)) {
                    it.remove();
                }
            }
        }
        if (libs.size() > 0) {
            // force descriptor for additinal libs since it shouldn't occur often and can save some time
            final IAnnotationFinder finder = new AnnotationFinder(new ConfigurableClasspathArchive(appModule.getClassLoader(), true, libs));
            for (Class<? extends Annotation> mclazz : mbeanClasses) {
                for (Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(mclazz)) {
                    final String name = clazz.get().getName();
                    if (done.contains(name)) {
                        continue;
                    }

                    appModule.getAdditionalLibMbeans().add(name);
                }
            }
        }

        return appModule;
    }
}
