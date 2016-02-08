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

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.configuration.ApplicationComposer;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Descriptor;
import org.apache.openejb.testing.Descriptors;
import org.apache.openejb.testing.JaxrsProviders;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filters;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

// use temp classloader so be careful with the decorated class
//
// dont reuse ApplicationComposers to control what we support here + cleanup what we don't want to support (XModule -> test only)
public class ApplicationComposerDeployer implements DynamicDeployer {
    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        if (!appModule.isStandaloneModule()) {
            return appModule;
        }

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            if (ejbModule.getFinder() == null) {
                continue;
            }
            WebModule webModule = null;
            for (final WebModule web : appModule.getWebModules()) {
                if (!web.getModuleId().equals(ejbModule.getModuleId())) {
                    continue;
                }
                webModule = web;
                break;
            }
            if (webModule == null) {
                continue;
            }

            for (final Class<?> clazz : ejbModule.getFinder().findAnnotatedClasses(ApplicationComposer.class)) {
                final ApplicationComposer applicationComposer = clazz.getAnnotation(ApplicationComposer.class);

                final Descriptor descriptor = clazz.getAnnotation(Descriptor.class);
                if (descriptor != null) {
                    configureDescriptor(appModule, descriptor);
                }
                final Descriptors descriptors = clazz.getAnnotation(Descriptors.class);
                if (descriptors != null) {
                    for (final Descriptor d : descriptors.value()) {
                        configureDescriptor(appModule, descriptor);
                    }
                }

                final Classes classes = clazz.getAnnotation(Classes.class);
                if (classes != null) {
                    configureClasses(webModule, ejbModule, applicationComposer, classes);
                }

                Object instance = null;
                final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(org.apache.openejb.util.Classes.ancestors(clazz)));
                for (final Method m : finder.findAnnotatedMethods(org.apache.openejb.testing.Module.class)) {
                    instance = configureModule(appModule, ejbModule, clazz, instance, m);
                }
                for (final Method m : finder.findAnnotatedMethods(Configuration.class)) {
                    instance = configureConfiguration(appModule, clazz, instance, m);
                }

                final JaxrsProviders jaxrsProviders = clazz.getAnnotation(JaxrsProviders.class);
                if (jaxrsProviders != null) {
                    for (final Class<?> c : jaxrsProviders.value()) {
                        webModule.getJaxrsProviders().add(c.getName());
                    }
                }
            }
        }
        return appModule;
    }

    private Object configureConfiguration(final AppModule appModule, final Class<?> clazz, Object instance, final Method m) {
        final int modifiers = m.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            throw new IllegalArgumentException("@Configuration should be public");
        }
        final boolean isStatic = Modifier.isStatic(modifiers);
        if (!isStatic) {
            try {
                instance = clazz.newInstance();
            } catch (final Exception e) {
                // no-op
            }
        }
        try {
            final Object result = m.invoke(isStatic ? null : instance);
            if (Properties.class.isInstance(result)) {
                appModule.getProperties().putAll(Properties.class.cast(result));
            } else {
                throw new IllegalArgumentException(result + " not yet supported (" + m + ")");
            }
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return instance;
    }

    private Object configureModule(final AppModule appModule, final EjbModule ejbModule, final Class<?> clazz, Object instance, final Method m) {
        final int modifiers = m.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            throw new IllegalArgumentException("@Module should be public");
        }
        final boolean isStatic = Modifier.isStatic(modifiers);
        if (!isStatic) {
            try {
                instance = clazz.newInstance();
            } catch (final Exception e) {
                // no-op
            }
        }
        try {
            final Object result = m.invoke(isStatic ? null : instance);
            if (EjbJar.class.isInstance(result)) {
                ejbModule.setEjbJar(EjbJar.class.cast(result));
            } else if (Persistence.class.isInstance(result)) {
                final Persistence persistence = Persistence.class.cast(result);
                if (!persistence.getPersistenceUnit().isEmpty()) {
                    appModule.getPersistenceModules().add(new PersistenceModule(appModule, rootUrl(ejbModule), persistence));
                }
            } else if (PersistenceUnit.class.isInstance(result)) {
                final PersistenceUnit unit = PersistenceUnit.class.cast(result);
                appModule.addPersistenceModule(new PersistenceModule(appModule, rootUrl(ejbModule), new Persistence(unit)));
            } else if (Beans.class.isInstance(result)) {
                final Beans beans = Beans.class.cast(result);
                ejbModule.setBeans(beans);
            } else {
                throw new IllegalArgumentException(result + " not yet supported (" + m + ")");
            }
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return instance;
    }

    private String rootUrl(final EjbModule ejbModule) {
        try {
            return ejbModule.getModuleUri().toURL().toExternalForm();
        } catch (final Exception e) { // malformed, npe...shouldn't occur at this point
            return null; // actually would be nicer to return null by default to skip any other scanning but needed by some provider and us sometimes
        }
    }

    private void configureClasses(final WebModule web, final EjbModule ejbModule,
                                  final ApplicationComposer applicationComposer, final Classes classes) {
        ejbModule.getEjbJar().setMetadataComplete(applicationComposer.metadataComplete());

        final Collection<Archive> archives = new LinkedList<>();
        if (classes.value().length > 0) {
            archives.add(new ClassesArchive(classes.value()));
        }
        if (classes.cdi()) {
            final Beans beans = new Beans();
            for (final Class<?> c : classes.cdiAlternatives()) {
                beans.addAlternativeClass(c);
            }
            for (final Class<?> c : classes.cdiDecorators()) {
                beans.addDecorator(c);
            }
            for (final Class<?> c : classes.cdiInterceptors()) {
                beans.addInterceptor(c);
            }
            ejbModule.setBeans(beans);

            if (applicationComposer.metadataComplete()) {
                for (final Class<?> c : classes.value()) {
                    beans.addManagedClass(null, c.getName());
                }

                final String name = BeanContext.Comp.openejbCompName(web.getModuleId());
                final org.apache.openejb.jee.ManagedBean managedBean = new CompManagedBean(name, BeanContext.Comp.class);
                managedBean.setTransactionType(TransactionType.BEAN);
                ejbModule.getEjbJar().addEnterpriseBean(managedBean);
            }
        }

        final CompositeArchive archive = new CompositeArchive(archives);
        final Archive finalArchive = classes.excludes().length > 0 ? new FilteredArchive(archive, Filters.invert(Filters.prefixes(classes.excludes()))) : archive;
        ejbModule.setFinder(new FinderFactory.OpenEJBAnnotationFinder(finalArchive).link());

        web.setFinder(ejbModule.getFinder());
        web.getWebApp().setMetadataComplete(ejbModule.getEjbJar().isMetadataComplete());
    }

    private void configureDescriptor(final AppModule appModule, final Descriptor descriptor) {
        URL resource = appModule.getClassLoader().getResource(descriptor.path());
        try {
            appModule.getAltDDs().put(descriptor.name(), resource == null ? new File(descriptor.path()).toURI().toURL() : resource);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
