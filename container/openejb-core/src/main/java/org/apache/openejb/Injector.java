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

package org.apache.openejb;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.inject.OWBInjector;

/**
 * Extracted from the OpenEjbContainer class
 */
public class Injector {

    private static Logger logger; // initialized lazily to get the logging config from properties

    public static <T> T inject(final T object) {

        assert object != null;

        final Class<?> clazz = object.getClass();

        final BeanContext context = resolve(clazz);

        if (context != null) { // found the test class directly
            final InjectionProcessor processor = new InjectionProcessor(object, context.getInjections(), context.getJndiContext());
            cdiInjections(context, object);
            try {
                return (T) processor.createInstance();
            } catch (final OpenEJBException e) {
                throw new InjectionException(clazz.getName(), e);
            }
        } else if (!isAnnotatedLocalClient(clazz)) { // nothing to do
            throw new NoInjectionMetaDataException(clazz.getName());
        }

        // the test class was not found in beans (OpenEJB ran from parent) but was annotated @LocalClient
        try {
            final InjectionProcessor<?> processor = ClientInjections.clientInjector(object);
            cdiInjections(null, object);
            return (T) processor.createInstance();
        } catch (final OpenEJBException e) {
            throw new NoInjectionMetaDataException("Injection failed", e);
        }
    }

    private static <T> void cdiInjections(final BeanContext context, final T object) {
        if (context == null || context.getWebBeansContext() == null) {
            return;
        }

        ThreadContext oldContext = null;
        final ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
        oldContext = ThreadContext.enter(callContext);
        try {
            OWBInjector.inject(context.getWebBeansContext().getBeanManagerImpl(), object, null);
        } catch (final Throwable t) {
            logger().warning("an error occured while injecting the class '" + object.getClass().getName() + "': " + t.getMessage());
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

    private static boolean isAnnotatedLocalClient(final Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            if (current.getAnnotation(LocalClient.class) != null) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    public static BeanContext resolve(Class<?> clazz) {

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        while (clazz != null && clazz != Object.class) {

            {
                final BeanContext context = containerSystem.getBeanContext(clazz.getName());

                if (context != null) {
                    return context;
                }
            }

            for (final BeanContext context : containerSystem.deployments()) {

                if (clazz == context.getBeanClass()) {
                    return context;
                }

            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    private static Logger logger() { // don't trigger init too eagerly to be sure to be configured
        if (logger == null) {
            logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, OpenEjbContainer.class);
        }
        return logger;
    }

    public static class NoInjectionMetaDataException extends IllegalStateException {
        public NoInjectionMetaDataException(final String s) {
            this(s, null);
        }

        public NoInjectionMetaDataException(final String s, final Exception e) {
            super(String.format("%s : Annotate the class with @%s so it can be discovered in the application scanning process", s, jakarta.annotation.ManagedBean.class.getName()), e);
        }
    }

    public static class InjectionException extends IllegalStateException {
        public InjectionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
