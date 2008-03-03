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
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import javax.naming.NamingException;
import java.lang.reflect.Method;

public class TomcatThreadContextListener implements ThreadContextListener {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");
    private static final String OPENEJB_CONTEXT = "OpenEJBContext";
    protected Method method;

    public TomcatThreadContextListener() {
        ContextBindings.bindContext(OPENEJB_CONTEXT, new OpenEJBContext());
        try {
            // someone decided to make the getThreadName package protected so we have to use reflection
            method = ContextBindings.class.getDeclaredMethod("getThreadName");
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            logger.error("Expected ContextBinding to have the method getThreadName()");
        }
    }

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


    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        // unbind the new context
        ContextBindings.unbindThread(OPENEJB_CONTEXT);

        // attempt to restore the old context
        Data data = exitedContext.get(Data.class);
        if (data != null && data.oldContextName != null) {
            try {
                ContextBindings.bindThread(data.oldContextName);
            } catch (NamingException e) {
            }
        }
    }

    private Object getThreadName() throws NamingException {
        // someone decided to make the getThreadName package protected so we have to use reflection
        try {
            Object threadName = method.invoke(null);
            return threadName;
        } catch (Exception e) {
            return null;
        }
    }

    private static class Data {
        private Object oldContextName;

        public Data(Object oldContext) {
            this.oldContextName = oldContext;
        }
    }
}
