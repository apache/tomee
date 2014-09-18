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

package org.apache.openejb.arquillian.common;

import org.apache.openejb.config.AdditionalBeanDiscoverer;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class TestClassDiscoverer implements AdditionalBeanDiscoverer {
    @Override
    public AppModule discover(final AppModule module) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final Set<Class<? extends Annotation>> testMarkers = new HashSet<>();
        for (final String s : asList("org.junit.Test", "org.testng.annotations.Test")) {
            try {
                testMarkers.add((Class<? extends Annotation>) contextClassLoader.loadClass(s));
            } catch (final Throwable e) {
                // no-op: deployment = false
            }
        }

        final Set<Class<?>> testClasses = new HashSet<>();
        if (!testMarkers.isEmpty()) {
            addTests(testMarkers, module.getEarLibFinder(), testClasses);
            for (final WebModule web : module.getWebModules()) {
                addTests(testMarkers, web.getFinder(), testClasses);
            }
            for (final EjbModule ejb : module.getEjbModules()) {
                addTests(testMarkers, ejb.getFinder(), testClasses);
            }
            for (final ConnectorModule connector : module.getConnectorModules()) {
                addTests(testMarkers, connector.getFinder(), testClasses);
            }
        }

        // keep it since CukeSpace doesn't rely on JUnit or TestNG @Test so it stays mandatory
        final File file = module.getFile();
        final String line = findTestName(file, module.getClassLoader());
        if (line != null) {
            String name;
            final int endIndex = line.indexOf('#');
            if (endIndex > 0) {
                name = line.substring(0, endIndex);
                if (file != null && !file.getName().equals(line.substring(endIndex + 1, line.length()))) {
                    name = null;
                }
            } else {
                name = line;
            }

            if (name != null) {
                try {
                    // call some reflection methods to make it fail if some dep are missing...
                    testClasses.add(module.getClassLoader().loadClass(name));
                } catch (final Throwable e) {
                    // no-op
                }
            }
        }

        final Iterator<Class<?>> it = testClasses.iterator();
        while (it.hasNext()) {
            try {
                // call some reflection methods to make it fail if some dep are missing...
                Class<?> current = it.next();

                if (!AnnotationDeployer.isInstantiable(current)) {
                    it.remove();
                    continue;
                }

                while (current != null) {
                    current.getDeclaredFields();
                    current.getDeclaredMethods();
                    current.getCanonicalName();
                    current = current.getSuperclass();
                    // TODO: more validations
                }
            } catch (final NoClassDefFoundError ncdfe) {
                it.remove();
            }
        }

        for (final Class<?> test : testClasses) {
            final EjbJar ejbJar = new EjbJar();
            final OpenejbJar openejbJar = new OpenejbJar();
            final String name = test.getName();
            final String ejbName = module.getModuleId() + "_" + name;
            final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(ejbName, name, true));
            bean.localBean();
            bean.setTransactionType(TransactionType.BEAN);
            final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
            ejbDeployment.setDeploymentId(ejbName);
            module.getEjbModules().add(new EjbModule(ejbJar, openejbJar));
        }

        return module;
    }

    private static void addTests(final Set<Class<? extends Annotation>> testMarkers, final IAnnotationFinder finder, final Set<Class<?>> testClasses) {
        if (finder == null) {
            return;
        }
        for (final Class<? extends Annotation> marker : testMarkers) {
            final List<Method> annotatedMethods = finder.findAnnotatedMethods(marker);
            for (final Method m : annotatedMethods) {
                try {
                    testClasses.add(m.getDeclaringClass());
                } catch (final NoClassDefFoundError e) {
                    // no-op
                }
            }
        }
    }

    private String findTestName(final File folder, final ClassLoader classLoader) {
        InputStream is = null;

        File dir = folder;

        if (dir != null && (dir.getName().endsWith(".war") || dir.getName().endsWith(".ear"))) {
            final File unpacked = new File(dir.getParentFile(), dir.getName().substring(0, dir.getName().length() - 4));
            if (unpacked.exists()) {
                dir = unpacked;
            }
        }

        if (dir != null && dir.isDirectory()) {
            final File info = new File(dir, "arquillian-tomee-info.txt");
            if (info.exists()) {
                try {
                    is = new FileInputStream(info);
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if (is == null) {
            is = classLoader.getResourceAsStream("arquillian-tomee-info.txt");
        }

        if (is != null) {
            try {
                return org.apache.openejb.loader.IO.slurp(is);
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                org.apache.openejb.loader.IO.close(is);
            }
        }
        return null;
    }
}
