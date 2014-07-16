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

package org.apache.openejb.core.ivm.naming.java;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.ContextHandler;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class javaURLContextFactory implements ObjectFactory {

    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable env) throws NamingException {
        return getContext();
    }

    public static Context getContext() {
        final ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext == null) {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final ClassLoader current = Thread.currentThread().getContextClassLoader();
            final Context globalContext = containerSystem.getJNDIContext();
            if (current == null) {
                return globalContext;
            }

            for (final AppContext appContext : containerSystem.getAppContexts()) {
                for (final WebContext web : appContext.getWebContexts()) { // more specific first
                    if (current.equals(web.getClassLoader())) {
                        return new ContextHandler(web.getJndiEnc());
                    }
                }
                if (current.equals(appContext.getClassLoader())) {
                    return new ContextHandler(appContext.getAppJndiContext());
                }
            }

            return globalContext;
        }

        final BeanContext di = callContext.getBeanContext();
        if (di != null) {
            return di.getJndiEnc();
        } else {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            return containerSystem.getJNDIContext();
        }
    }

    private static class ContextWithglobalFallbackWrapper extends ContextWrapper {
        public ContextWithglobalFallbackWrapper(final Context first) {
            super(first);
        }

        @Override
        public Object lookup(final Name name) throws NamingException {
            try {
                return super.lookup(name);
            } catch (final NameNotFoundException nnfe) {
                try {
                    return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(name);
                } catch (final NameNotFoundException nnfe2) {
                    // ignore, let it be thrown
                }
                throw nnfe;
            }
        }

        @Override
        public Object lookup(final String name) throws NamingException {
            try {
                return super.lookup(name);
            } catch (final NameNotFoundException nnfe) {
                try {
                    return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(name);
                } catch (final NameNotFoundException nnfe2) {
                    // ignore, let it be thrown
                }
                throw nnfe;
            }
        }
    }
}
