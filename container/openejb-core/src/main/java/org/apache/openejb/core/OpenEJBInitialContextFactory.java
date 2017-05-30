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

package org.apache.openejb.core;

import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OpenEJBInitialContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(final Hashtable env) throws NamingException {
        // don't validate there env content, it is commonly built from System properties and therefore can inherit a bunch of things
        return new LocalFallbackContextWrapper(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext());
    }

    private static final class LocalFallbackContextWrapper extends ContextWrapper {
        private final ConcurrentMap<String, String> mapping = new ConcurrentHashMap<>();

        private LocalFallbackContextWrapper(final Context jndiContext) {
            super(jndiContext);
        }

        @Override
        public Object lookup(final String userName) throws NamingException {
            String jndi = mapping.get(userName);
            if (jndi == null) {
                jndi = userName;
            }
            try {
                return super.lookup(jndi);
            } catch (final NameNotFoundException nnfe) {
                if (!jndi.startsWith("java:") && !jndi.startsWith("openejb:")) { // try jndi lookup
                    try {
                        final String ejb = "java:openejb/local/" + jndi;
                        final Object lookup = super.lookup(ejb);
                        mapping.put(userName, ejb);
                        return lookup;
                    } catch (final NameNotFoundException nnfeIgnored) { // resource
                        try {
                            final String resource = "java:openejb/Resource/" + jndi;
                            final Object resourceInstance = super.lookup(resource);
                            mapping.put(userName, resource);
                            return resourceInstance;
                        } catch (final NameNotFoundException nnfeIgnoredAgain) {
                            throw nnfe;
                        }
                    }
                }
                throw nnfe;
            }
        }
    }
}