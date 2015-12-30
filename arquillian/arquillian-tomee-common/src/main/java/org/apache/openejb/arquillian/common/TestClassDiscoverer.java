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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class TestClassDiscoverer implements AdditionalBeanDiscoverer {
    @Override
    public AppModule discover(final AppModule module) {
        final Set<Class<?>> testClasses = new HashSet<>();
        final Map<Class<?>, WebModule> webTestClasses = new HashMap<>();
        final Set<ClassLoader> saw = new HashSet<>();
        if (module.getClassLoader() != null) {
            addTests(findMarkers(module.getClassLoader()), findClassMarkers(module.getClassLoader()), module.getEarLibFinder(), testClasses);
            saw.add(module.getClassLoader());
        }
        for (final WebModule web : module.getWebModules()) {
            if (web.getClassLoader() != null && !saw.contains(web.getClassLoader())) {
                final Set<Class<?>> classes = new HashSet<>();
                addTests(findMarkers(web.getClassLoader()), findClassMarkers(web.getClassLoader()), web.getFinder(), classes);
                saw.add(web.getClassLoader());
                for (final Class<?> c : classes) {
                    webTestClasses.put(c, web);
                }

                // in case of an ear if we find the same test class in a webapp we don't want it in lib part
                // this case can happen in tomee-embedded mainly
                final Iterator<Class<?>> c = testClasses.iterator();
                while (c.hasNext()) {
                    final String cl = c.next().getName();
                    for (final Class<?> wc : classes) {
                        if (cl.equals(wc.getName())) {
                            c.remove();
                            break;
                        }
                    }
                }
                testClasses.addAll(classes);
            }
        }
        for (final EjbModule ejb : module.getEjbModules()) {
            if (ejb.getClassLoader() != null && !saw.contains(ejb.getClassLoader())) {
                addTests(findMarkers(ejb.getClassLoader()), findClassMarkers(ejb.getClassLoader()), ejb.getFinder(), testClasses);
                saw.add(ejb.getClassLoader());
            }
        }
        for (final ConnectorModule connector : module.getConnectorModules()) {
            if (connector.getClassLoader() != null && !saw.contains(connector.getClassLoader())) {
                addTests(findMarkers(connector.getClassLoader()), findClassMarkers(connector.getClassLoader()), connector.getFinder(), testClasses);
                saw.add(connector.getClassLoader());
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
                boolean found = false;
                for (final WebModule web : module.getWebModules()) {
                    try {
                        testClasses.add(web.getClassLoader().loadClass(name));
                        found = true;
                        break;
                    } catch (final Throwable e) {
                        // no-op
                    }
                }
                if (!found) {
                    try {
                        testClasses.add(module.getClassLoader().loadClass(name));
                    } catch (final Throwable e) {
                        // no-op
                    }
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

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
            ejbModule.setClassLoader(test.getClassLoader());
            final WebModule webModule = webTestClasses.get(test);
            if (webModule != null) {
                ejbModule.setWebapp(true);
                ejbModule.getProperties().put("openejb.ejbmodule.webappId", webModule.getModuleId());
            }
            ejbModule.getProperties().put("openejb.ejbmodule.MergeWebappJndiContext", "true");
            module.getEjbModules().add(ejbModule);
        }

        return module;
    }

    private Set<Class<? extends Annotation>> findClassMarkers(final ClassLoader contextClassLoader) {
        final Set<Class<? extends Annotation>> testMarkers = new HashSet<>();
        for (final String s : asList("org.junit.runner.RunWith", Discover.class.getName())) {
            try {
                testMarkers.add((Class<? extends Annotation>) contextClassLoader.loadClass(s));
            } catch (final Throwable e) {
                // no-op
            }
        }
        return testMarkers;
    }

    private Set<Class<? extends Annotation>> findMarkers(final ClassLoader contextClassLoader) {
        final Set<Class<? extends Annotation>> testMarkers = new HashSet<>();
        for (final String s : asList("org.junit.Test", "org.testng.annotations.Test")) {
            try {
                testMarkers.add((Class<? extends Annotation>) contextClassLoader.loadClass(s));
            } catch (final Throwable e) {
                // no-op: deployment = false
            }
        }
        return testMarkers;
    }

    private static void addTests(final Set<Class<? extends Annotation>> testMarkers, final Set<Class<? extends Annotation>> classMarkers,
                                 final IAnnotationFinder finder, final Set<Class<?>> testClasses) {
        if (finder == null) {
            return;
        }
        for (final Class<? extends Annotation> marker : testMarkers) {
            try {
                final List<Method> annotatedMethods = finder.findAnnotatedMethods(marker);
                for (final Method m : annotatedMethods) {
                    try {
                        testClasses.add(m.getDeclaringClass());
                    } catch (final NoClassDefFoundError e) {
                        // no-op
                    }
                }
            } catch (final NoClassDefFoundError ncdfe) {
                // no-op
            }
        }
        for (final Class<? extends Annotation> marker : classMarkers) {
            try {
                testClasses.addAll(finder.findAnnotatedClasses(marker));
            } catch (final NoClassDefFoundError ncdfe) {
                // no-op
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
                    // no-op
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
                // no-op
            } finally {
                org.apache.openejb.loader.IO.close(is);
            }
        }
        return null;
    }
}
