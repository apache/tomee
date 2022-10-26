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
package org.apache.tomee.catalina;

import org.apache.naming.ContextBindings;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Tomcat thread context listener.
 *
 * @version $Rev$ $Date$
 */
public class TomcatThreadContextListener implements ThreadContextListener {

    /**
     * Logger instance for tomcat
     */
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    /**
     * OpenEJB context name
     */
    private static final String OPENEJB_CONTEXT = "OpenEJBContext";

    /**
     * getThreadName method in class ContextBindings
     */
    protected Method method;
    private Map<Thread, Object> threadNameBindings;

    /**
     * Creates a new instance.
     */
    public TomcatThreadContextListener() {
        ContextBindings.bindContext(OPENEJB_CONTEXT, new OpenEJBContext());
        try {
            // someone decided to make the getThreadName package protected so we have to use reflection
            method = ContextBindings.class.getDeclaredMethod("getThreadName");
            method.setAccessible(true);

            final Field threadNameBindingsField = ContextBindings.class.getDeclaredField("threadObjectBindings");
            threadNameBindingsField.setAccessible(true);
            threadNameBindings = (Map<Thread, Object>) threadNameBindingsField.get(null);
        } catch (final Exception e) {
            LOGGER.error("Expected ContextBinding to have the method getThreadName()");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
        // save off the old context if possible
        try {
            final Data data = new Data(getThreadName());
            newContext.set(Data.class, data);
        } catch (final NamingException ignored) {
            // no-op
        }

        // set the new context
        try {
            ContextBindings.bindThread(OPENEJB_CONTEXT, null);
        } catch (final NamingException e) {
            ContextBindings.unbindContext(OPENEJB_CONTEXT, null);
            throw new IllegalArgumentException("Unable to bind OpenEJB enc");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
        // unbind the new context
        ContextBindings.unbindThread(OPENEJB_CONTEXT, null);

        // attempt to restore the old context
        final Data data = exitedContext.get(Data.class);
        if (data != null && data.oldContextName != null) {
            try {
                ContextBindings.bindThread(data.oldContextName, null);
            } catch (final NamingException e) {
                LOGGER.error("Exception in method contextExited", e);
            }
        }
    }

    /**
     * Gets thread name.
     *
     * @return thread name
     * @throws NamingException for exception
     */
    private Object getThreadName() throws NamingException {
        try {
            return threadNameBindings.get(Thread.currentThread());
        } catch (final Exception e) {
            // no-op: try the old implementation
        }

        // this implementation is probably better but slower
        try {
            return method.invoke(null);

        } catch (final InvocationTargetException e) {
            // if it's a naming exception, it should be treated by the caller
            if (e.getCause() != null && e.getCause() instanceof NamingException) {
                throw (NamingException) e.getCause();
            }

            LOGGER.error("Exception in method getThreadName", e);
            return null;

        } catch (final Exception e) {
            LOGGER.error("Exception in method getThreadName", e);
            return null;
        }
    }

    //Internal stuff to hold old context name
    private static class Data {
        private Object oldContextName;

        public Data(final Object oldContextName) {
            this.oldContextName = oldContextName;
        }
    }
}
