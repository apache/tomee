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

import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class TomcatResourceFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomcatResourceFactory.class);

    private String jndiName;
    private String appName;
    private String factory;
    private Reference reference;

    public void setJndiName(final String jndiName) {
        this.jndiName = jndiName;
    }

    public void setAppName(final String appName) {
        this.appName = appName;
    }

    public void setFactory(final String factory) {
        this.factory = factory;
    }

    public void setReference(final Reference reference) {
        this.reference = reference;
    }

    public Object create() throws NamingException {
        final TomcatWebAppBuilder.ContextInfo info = ((TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class))
                .getContextInfo(appName);
        if (info == null || info.standardContext == null) {
            return null;
        }

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final ClassLoader tccl = info.standardContext.getLoader().getClassLoader();
        Thread.currentThread().setContextClassLoader(tccl);
        try {
            // lookup can't work because of the lifecycle
            // return new InitialContext().lookup(jndiName);

            if (factory != null) {
                final Class<?> clazz = tccl.loadClass(factory);
                final Object instance = clazz.newInstance();
                if (instance instanceof ObjectFactory) {
                    // not really used as expected but it matches a bit more than before
                    // context is null since it can't be used at this moment (see TomcatWebAppBuilder lifecycle)
                    return ((ObjectFactory) instance).getObjectInstance(reference, new CompositeName(jndiName), null, null);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Can't create resource " + jndiName, e);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }

        return null;
    }
}
