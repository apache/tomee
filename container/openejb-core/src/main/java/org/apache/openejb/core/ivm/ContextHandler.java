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

package org.apache.openejb.core.ivm;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ContextHandler extends ContextWrapper {
    public ContextHandler(final Context jndiContext) {
        super(jndiContext);
    }

    @Override
    public Object lookup(final Name name) throws NamingException {
        try {
            return context.lookup(name);
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
            if ("java:".equals(name)) {
                return context;
            }
            return context.lookup(name);
        } catch (final UndeclaredThrowableException ute) {
            Throwable e = ute.getUndeclaredThrowable();
            while (e != null) {
                if (InvocationTargetException.class.isInstance(e)) {
                    final Throwable unwrap = InvocationTargetException.class.cast(e).getCause();
                    if (e == unwrap) {
                        throw new NameNotFoundException(name);
                    }
                    e = unwrap;
                } else if (UndeclaredThrowableException.class.isInstance(e)) {
                    final Throwable unwrap = UndeclaredThrowableException.class.cast(e).getUndeclaredThrowable();
                    if (e == unwrap) {
                        throw new NameNotFoundException(name);
                    }
                    e = unwrap;
                } else {
                    break;
                }
                if (NameNotFoundException.class.isInstance(e)) {
                    throw NameNotFoundException.class.cast(e);
                }
            }
            throw new NameNotFoundException(name);
        } catch (final NameNotFoundException nnfe) {
            try {
                return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(name);
            } catch (final NameNotFoundException nnfe2) {
                // ignore, let it be thrown
            }
            try {
                final ThreadContext threadContext = ThreadContext.getThreadContext();
                if (threadContext != null) {
                    return threadContext.getBeanContext()
                            .getModuleContext().getModuleJndiContext()
                            .lookup(name);
                }
            } catch (final Exception nnfe3) {
                // ignore, let it be thrown
            }
            throw nnfe;
        }
    }
    
    public void setReadOnly() {
        if(IvmContext.class.isInstance(context)) {
            IvmContext.class.cast(context).setReadOnly(true);
        }
    }
}
