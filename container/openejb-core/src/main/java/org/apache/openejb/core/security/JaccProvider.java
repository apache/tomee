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
package org.apache.openejb.core.security;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

/**
 * @version $Rev$ $Date$
 */
public abstract class JaccProvider {

    private static final String FACTORY_NAME = JaccProvider.class.getName();
    private static JaccProvider jaccProvider;

    public static JaccProvider get() {
        return jaccProvider;
    }

    public static void set(JaccProvider provider) {
        // todo add a security check
        jaccProvider = provider;
    }

    /**
     * This static method uses a system property to find and instantiate (via a
     * public constructor) a provider specific factory implementation class.
     * The name of the provider specific factory implementation class is
     * obtained from the value of the system property,<p>
     * <code>org.apache.openejb.security.JaccProvider</code>.
     * PolicyConfigurationFactory implementation class.
     *
     * @throws ClassNotFoundException when the class named by the system
     *                                property could not be found including because the value of the system
     *                                property has not be set.
     * @throws PolicyContextException if the implementation throws a checked
     *                                exception that has not been accounted for by the
     *                                getPolicyConfigurationFactory method signature. The exception thrown by
     *                                the implementation class will be encapsulated (during construction) in
     *                                the thrown PolicyContextException
     */
    public static void install() throws ClassNotFoundException, PolicyContextException {
        if (jaccProvider != null) return;

        final String[] factoryClassName = {null};
        try {
            jaccProvider = (JaccProvider) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    factoryClassName[0] = System.getProperty(FACTORY_NAME);

                    if (factoryClassName[0] == null)
                        throw new ClassNotFoundException("Property " + FACTORY_NAME + " not set");
                    Thread currentThread = Thread.currentThread();
                    ClassLoader tccl = currentThread.getContextClassLoader();
                    return Class.forName(factoryClassName[0], true, tccl).newInstance();
                }
            });
        } catch (PrivilegedActionException pae) {
            if (pae.getException() instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) pae.getException();
            } else if (pae.getException() instanceof InstantiationException) {
                throw new ClassNotFoundException(factoryClassName[0] + " could not be instantiated");
            } else if (pae.getException() instanceof IllegalAccessException) {
                throw new ClassNotFoundException("Illegal access to " + factoryClassName);
            }
            throw new PolicyContextException(pae.getException());
        }
    }


    public static class Factory extends PolicyConfigurationFactory {
        public Factory() throws PolicyContextException, ClassNotFoundException {
            install();
        }

        public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
            return get().getPolicyConfiguration(contextID, remove);
        }

        public boolean inService(String contextID) throws PolicyContextException {
            return get().inService(contextID);
        }
    }

    public static class Policy extends java.security.Policy {

        public Policy() throws PolicyContextException, ClassNotFoundException {
            install();
        }

        public PermissionCollection getPermissions(CodeSource codesource) {
            return get().getPermissions(codesource);
        }

        public void refresh() {
            get().refresh();
        }

        public boolean implies(ProtectionDomain domain, Permission permission) {
            return get().implies(domain, permission);
        }
    }

    public abstract PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException;

    public abstract boolean inService(String contextID) throws PolicyContextException;

    public abstract PermissionCollection getPermissions(CodeSource codesource);

    public abstract void refresh();

    public abstract boolean implies(ProtectionDomain domain, Permission permission);
}
