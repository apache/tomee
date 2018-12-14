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
package org.apache.tomee.common;

import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Strings;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
import java.util.Properties;

public abstract class AbstractObjectFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance(final Object object, final Name name, final Context context, final Hashtable environment) throws Exception {
        final Reference ref = (Reference) object;

        // the jndi context to use for the lookup (usually null which is the default context)
        final String jndiProviderId = NamingUtil.getProperty(ref, NamingUtil.JNDI_PROVIDER_ID);

        // the jndi name
        String jndiName = NamingUtil.getProperty(ref, NamingUtil.JNDI_NAME);
        if (jndiName == null) {
            jndiName = buildJndiName(ref);
        }

        // look up the reference
        try {
            return lookup(jndiProviderId, jndiName);
        } catch (final NameNotFoundException nnfe) { // EE.5.18: try using java:module/<shortName> prefix

            // 2nd try
            if (jndiName.startsWith("java:")) {
                try {
                    return new InitialContext().lookup(jndiName);
                } catch (final NameNotFoundException ignored) {
                    // no-op
                } catch (final NoInitialContextException nice) {
                    final Properties props = new Properties();
                    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
                    try {
                        return new InitialContext(props).lookup(jndiName);
                    } catch (final NameNotFoundException ignored) {
                        // no-op
                    }
                }
            }

            // 3rd try
            if (ref.getClassName() != null) {
                final String moduleName = "java:module/" + Strings.lastPart(ref.getClassName(), '.');
                try {
                    return new InitialContext().lookup(moduleName);
                } catch (final NameNotFoundException ignored) {
                    // no-op
                } catch (final NoInitialContextException nice) {
                    final Properties props = new Properties();
                    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
                    try {
                        return new InitialContext(props).lookup(moduleName);
                    } catch (final NameNotFoundException ignored) {
                        // no-op
                    }
                }
            }

            throw nnfe;
        }
    }

    protected abstract String buildJndiName(Reference reference) throws NamingException;

    protected Object lookup(final String jndiProviderId, final String jndiName) throws NamingException {
        final Context externalContext = getContext(jndiProviderId);
        synchronized (externalContext) {
            /* According to the JNDI SPI specification multiple threads may not access the same JNDI
            Context *instance* concurrently. Since we don't know the origins of the federated context we must
            synchronize access to it. JNDI SPI Specification 1.2 Section 2.2
            */
            return externalContext.lookup(jndiName);
        }
    }

    protected Context getContext(final String jndiProviderId) throws NamingException {
        if (jndiProviderId != null) {
            final String contextJndiName = "java:openejb/remote_jndi_contexts/" + jndiProviderId;
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final Context context = (Context) containerSystem.getJNDIContext().lookup(contextJndiName);
            return context;
        } else {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final Context context = containerSystem.getJNDIContext();
            return context;
        }
    }

}
