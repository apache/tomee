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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.naming.CrossClassLoaderJndiReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Messages;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class LazyEjbReference extends Reference {

    private static final Messages messages = new Messages(LazyEjbReference.class);

    private final EjbResolver.Reference info;

    private Reference reference;
    private final URI moduleUri;
    private final boolean useCrossClassLoaderRef;

    public LazyEjbReference(final EjbResolver.Reference info, final URI moduleUri, final boolean useCrossClassLoaderRef) {
        super();
        this.info = info;
        this.moduleUri = moduleUri;
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public Object getObject() throws NamingException {
        if (reference != null) {
            return reference.getObject();
        }

        final SystemInstance systemInstance = SystemInstance.get();


        final EjbResolver resolver = systemInstance.getComponent(EjbResolver.class);

        final String deploymentId = resolver.resolve(info, moduleUri);

        if (deploymentId == null) {
            String key = "lazyEjbRefNotResolved";
            if (info.getHome() != null) {
                key += ".home";
            }
            final String message = messages.format(key, info.getName(), info.getEjbLink(), info.getHome(), info.getInterface());
            throw new NameNotFoundException(message);
        }

        final ContainerSystem containerSystem = systemInstance.getComponent(ContainerSystem.class);

        final BeanContext beanContext = containerSystem.getBeanContext(deploymentId);

        if (beanContext == null) {
            final String message = messages.format("deploymentNotFound", info.getName(), deploymentId);
            throw new NameNotFoundException(message);
        }

        InterfaceType type = null;
        switch (info.getRefType()) {
            case LOCAL:
                type = InterfaceType.BUSINESS_LOCAL;
                break;
            case REMOTE:
                type = InterfaceType.BUSINESS_REMOTE;
                break;
        }

        final String jndiName = "openejb/Deployment/" + JndiBuilder.format(deploymentId, info.getInterface(), type);

        if (useCrossClassLoaderRef && isRemote(beanContext)) {
            reference = new CrossClassLoaderJndiReference(jndiName);
        } else {
            reference = new IntraVmJndiReference(jndiName);
        }

        return reference.getObject();
    }

    private boolean isRemote(final BeanContext beanContext) {
        switch (info.getRefType()) {
            case REMOTE:
                return true;
            case LOCAL:
                return false;
            case UNKNOWN: {
                for (final Class clazz : beanContext.getInterfaces(InterfaceType.BUSINESS_REMOTE)) {
                    if (clazz.getName().equals(info.getInterface())) {
                        return true;
                    }
                }
            }
            default:
                return false;
        }
    }
}
