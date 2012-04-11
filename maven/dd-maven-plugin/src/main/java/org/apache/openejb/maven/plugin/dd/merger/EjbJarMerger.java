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
package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ContainerConcurrency;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class EjbJarMerger extends Merger<EjbJar> {
    public EjbJarMerger(final Log logger) {
        super(logger);
    }

    @Override
    public EjbJar merge(final EjbJar reference, final EjbJar toMerge) {
        for (EnterpriseBean bean : toMerge.getEnterpriseBeans()) {
            if (reference.getEnterpriseBeansByEjbName().containsKey(bean.getEjbName())) {
                log.warn("bean " + bean.getEjbName() + " already defined");
            } else {
                reference.addEnterpriseBean(bean);
            }
        }

        for (Interceptor interceptor : toMerge.getInterceptors()) {
            if (reference.getInterceptor(interceptor.getInterceptorClass()) != null) {
                log.warn("interceptor " + interceptor.getInterceptorClass() + " already defined");
            } else {
                reference.addInterceptor(interceptor);
            }
        }

        final AssemblyDescriptor descriptor = toMerge.getAssemblyDescriptor();
        mergeAssemblyDescriptor(reference.getAssemblyDescriptor(), descriptor);

        return reference;
    }

    // TODO: merge it in a better way
    private void mergeAssemblyDescriptor(final AssemblyDescriptor reference, final AssemblyDescriptor descriptor) {
        for (SecurityRole role : descriptor.getSecurityRole()) {
            boolean found = false;
            for (SecurityRole refRole : reference.getSecurityRole()) {
                if (refRole.getRoleName().equals(role.getRoleName())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                log.warn("role " + role.getRoleName() + " already defined");
            } else {
                reference.getSecurityRole().add(role);
            }
        }

        for (MethodPermission perm : descriptor.getMethodPermission()) {
            boolean found = false;
            for (MethodPermission refPerm : reference.getMethodPermission()) {
                if (refPerm.getRoleName().equals(perm.getRoleName())) {
                    found  = true;
                    break;
                }
            }
            if (found) {
                log.warn("method permission " + perm.getId() + " already defined");
            } else {
                reference.getMethodPermission().add(perm);
            }
        }

        for (ContainerTransaction tx : descriptor.getContainerTransaction()) {
            reference.getContainerTransaction().add(tx);
        }

        for (ContainerConcurrency concurrency : descriptor.getContainerConcurrency()) {
            reference.getContainerConcurrency().add(concurrency);
        }

        for (InterceptorBinding interceptorBinding : descriptor.getInterceptorBinding()) {
            boolean found = false;
            for (InterceptorBinding refInterceptorBinding : reference.getInterceptorBinding()) {
                if (refInterceptorBinding.getEjbName().equals(interceptorBinding.getEjbName())) {
                    for (String interceptor : interceptorBinding.getInterceptorClass()) {
                        if (refInterceptorBinding.getInterceptorClass().contains(interceptor)) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
                log.warn("interceptor binding " + interceptorBinding.getId() + " already defined");
            } else {
                reference.getInterceptorBinding().add(interceptorBinding);
            }
        }

        for (MessageDestination destination : descriptor.getMessageDestination()) {
            boolean found = false;
            for (MessageDestination refDestination : reference.getMessageDestination()) {
                if (refDestination.getMessageDestinationName().equals(destination.getMessageDestinationName())) { // is id the good test?
                    found = true;
                    break;
                }
            }
            if (found) {
                log.warn("message destination " + destination.getMessageDestinationName() + " already defined");
            } else {
                reference.getMessageDestination().add(destination);
            }
        }

        for (ApplicationException exception : descriptor.getApplicationException()) {
            if (reference.getApplicationExceptionMap().containsKey(exception.getKey())) {
                log.warn("application exception " + exception.getKey() + " already defined");
            } else {
                reference.getApplicationException().add(exception);
            }
        }
    }

    @Override
    public EjbJar createEmpty() {
        return new EjbJar();
    }

    @Override
    public EjbJar read(URL url) {
        try {
            return (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, new BufferedInputStream(url.openStream()), false);
        } catch (Exception e) {
            return createEmpty();
        }
    }

    @Override
    public String descriptorName() {
        return "ejb-jar.xml";
    }

    @Override
    public void dump(final File dump, final EjbJar ejbJar) throws Exception {
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dump));
        try {
            JaxbJavaee.marshal(EjbJar.class, ejbJar, stream);
        } finally {
            stream.close();
        }
    }
}
