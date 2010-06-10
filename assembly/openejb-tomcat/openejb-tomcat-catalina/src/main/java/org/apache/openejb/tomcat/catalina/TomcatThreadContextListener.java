/**
 *
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
package org.apache.openejb.tomcat.catalina;

import org.apache.naming.ContextBindings;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import java.lang.reflect.Method;

/**
 * Tomcat thread context listener.
 *
 * @version $Rev$ $Date$
 */
public class TomcatThreadContextListener implements ThreadContextListener {

    /**
     * Logger instance for tomcat
     */
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    /**
     * OpenEJB context name
     */
    private static final String OPENEJB_CONTEXT = "OpenEJBContext";

    /**
     * getThreadName method in class ContextBindings
     */
    protected Method method;

    /**
     * Creates a new instance.
     */
    public TomcatThreadContextListener() {
        ContextBindings.bindContext(OPENEJB_CONTEXT, new OpenEJBContext());
        boolean accessible = false;
        try {
            // someone decided to make the getThreadName package protected so we have to use reflection
            method = ContextBindings.class.getDeclaredMethod("getThreadName");
            accessible = method.isAccessible();
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            logger.error("Expected ContextBinding to have the method getThreadName()");
        } finally {
            if (!accessible) {
                method.setAccessible(accessible);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        // save off the old context if possible
        try {
            Data data = new Data(getThreadName());
            newContext.set(Data.class, data);
        } catch (NamingException ignored) {
        }

        // set the new context
        try {
            ContextBindings.bindThread(OPENEJB_CONTEXT);
        } catch (NamingException e) {
            ContextBindings.unbindContext(OPENEJB_CONTEXT);
            throw new IllegalArgumentException("Unable to bind OpenEJB enc");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        // unbind the new context
        ContextBindings.unbindThread(OPENEJB_CONTEXT);

        // attempt to restore the old context
        Data data = exitedContext.get(Data.class);
        if (data != null && data.oldContextName != null) {
            try {
                ContextBindings.bindThread(data.oldContextName);
            } catch (NamingException e) {
                logger.error("Exception in method contextExited", e);
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
            Object threadName = method.invoke(null);
            return threadName;
        } catch (Exception e) {
            logger.error("Exception in method getThreadName", e);
            return null;
        }
    }

    //Internal stuff to hold old context name
    private static class Data {
        private Object oldContextName;

        public Data(Object oldContextName) {
            this.oldContextName = oldContextName;
        }
    }
}
