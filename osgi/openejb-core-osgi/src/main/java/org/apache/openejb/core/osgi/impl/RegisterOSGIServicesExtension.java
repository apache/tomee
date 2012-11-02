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

package org.apache.openejb.core.osgi.impl;

import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.DefaultLiteral;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RegisterOSGIServicesExtension implements Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOSGIServicesExtension.class);

    protected static Bundle current = null;

    public void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd) {
        if (current != null) {
            for (Bundle b : current.getBundleContext().getBundles()) {
                final ServiceReference[] services = b.getRegisteredServices();
                if (services != null) {
                    for (ServiceReference service  : services) {
                        String[] clazz = (String[]) service.getProperty("objectClass");
                        if (clazz == null) {
                            continue;
                        }

                        for (String name : clazz) {
                            try {
                                current.loadClass(name);
                                abd.addBean(new OSGiServiceBean<Object>(service));
                                LOGGER.debug("added service {} as a CDI Application scoped bean", name);
                                break;
                            } catch (NoClassDefFoundError ignored) {
                                // no-op
                            } catch (ClassNotFoundException e) {
                                // can't load the class so no need to register the service
                            }
                        }
                    }
                }
            }
        }
    }

    private static Class<?> serviceClass(final ServiceReference service) {
        final Bundle bundle = service.getBundle();
        if (bundle == null) {
            return null;
        }

        final BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext == null) {
            return null;
        }

        final Object instance;
        try {
            instance = bundleContext.getService(service);
        } catch (RuntimeException re) {
            return null;
        }

        if (instance == null) {
            return null;
        }

        return instance.getClass();
    }

    public static class OSGiServiceBean<T> implements Bean<T> {
        private static final Set<Annotation> QUALIFIERS;

        static {
            final Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add(new DefaultLiteral());
            qualifiers.add(new AnyLiteral());
            QUALIFIERS = Collections.unmodifiableSet(qualifiers);
        }

        private final ServiceReference service;
        private final Class<?> clazz;
        private final Set<Type> types = new HashSet<Type>();

        public OSGiServiceBean(final ServiceReference srv) {
            service = srv;
            clazz = serviceClass(service);

            for (String clazz : (String[]) service.getProperty(Constants.OBJECTCLASS)) {
                try {
                    types.add(service.getBundle().loadClass(clazz));
                } catch (ClassNotFoundException ignored) {
                    // no-op
                }
            }
        }

        @Override
        public T create(final CreationalContext<T> ctx) {
            return (T) service.getBundle().getBundleContext().getService(service);
        }

        @Override
        public void destroy(final T instance, final CreationalContext<T> ctx) {
            // no-op
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return QUALIFIERS;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public String getName() {
            return "OSGiService_" + service.getProperty(Constants.SERVICE_ID);
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return clazz;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return true;
        }
    }
}
