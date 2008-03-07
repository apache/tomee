/**
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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Messages;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.core.ivm.naming.CrossClassLoaderJndiReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;

import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
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

    public LazyEjbReference(EjbResolver.Reference info, URI moduleUri, boolean useCrossClassLoaderRef) {
        super();
        this.info = info;
        this.moduleUri = moduleUri;
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public Object getObject() throws NamingException {
        if (reference != null){
            return reference.getObject();
        }

        SystemInstance systemInstance = SystemInstance.get();


        EjbResolver resolver = systemInstance.getComponent(EjbResolver.class);

        String deploymentId = resolver.resolve(info, moduleUri);

        if (deploymentId == null) {
            String key = "lazyEjbRefNotResolved";
            if (info.getHome() != null){
                key += ".home";
            }
            String message = messages.format(key, info.getName(), info.getEjbLink(), info.getHome(), info.getInterface());
            throw new NameNotFoundException(message);
        }

        ContainerSystem containerSystem = systemInstance.getComponent(ContainerSystem.class);

        DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);

        if (deploymentId == null) {
            String message = messages.format("deploymentNotFound", info.getName(), deploymentId);
            throw new NameNotFoundException(message);
        }

        String jndiName = "java:openejb/Deployment/" + deploymentId + "/" + info.getInterface();

        if (useCrossClassLoaderRef && isRemote(deploymentInfo)) {
            reference = new CrossClassLoaderJndiReference(jndiName);
        } else {
            reference = new IntraVmJndiReference(jndiName);
        }

        return reference.getObject();
    }

    private boolean isRemote(DeploymentInfo deploymentInfo) {
        switch(info.getRefType()){
            case REMOTE: return true;
            case LOCAL: return false;
            case UNKNOWN:{
                for (Class clazz : deploymentInfo.getInterfaces(InterfaceType.BUSINESS_REMOTE)) {
                    if (clazz.getName().equals(info.getInterface())) return true;
                }
            };
            default: return false;
        }
    }
}
