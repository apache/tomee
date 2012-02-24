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

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RegisterOSGIServicesExtension implements Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOSGIServicesExtension.class);

    public void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd) {
        final Bundle[] bundles = OpenEJBBundleContextHolder.get().getBundles();
        for (Bundle bundle : bundles) {
            final ServiceReference[] services = bundle.getRegisteredServices();
            if (services != null) {
                for (ServiceReference service  : services) {
                    final Class<?> clazz = serviceClass(service);
                    abd.addBean(new OSGiServiceBean<Object>(service));
                    LOGGER.debug("added service {0} as a CDI Application scoped bean", clazz.getName());
                }
            }
        }
    }

    private static Class<Object> serviceClass(ServiceReference service) {
        return (Class<Object>) service.getBundle().getBundleContext().getService(service).getClass();
    }

    public static class OSGiServiceBean<T> implements Bean<T> {
        private final ServiceReference service;

        public OSGiServiceBean(final ServiceReference srv) {
            service = srv;
        }

        @Override
        public T create(CreationalContext<T> ctx) {
            return (T) service.getBundle().getBundleContext().getService(service);
        }

        @Override
        public void destroy(T instance, CreationalContext<T> ctx) {
            // no-op
        }

        @Override
        public Set<Type> getTypes() {
            final Set<Type> types = new HashSet<Type>();
            for (String clazz : (String[]) service.getProperty(Constants.OBJECTCLASS)) {
                try {
                    types.add(service.getBundle().loadClass(clazz));
                } catch (ClassNotFoundException ignored) {
                    // no-op
                }
            }
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            final Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add( new AnnotationLiteral<Default>() {} );
            qualifiers.add( new AnnotationLiteral<Any>() {} );
            return qualifiers;
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
            return service.getBundle().getBundleContext().getService(service).getClass();
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
