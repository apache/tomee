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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb;

import java.lang.reflect.Method;

import javax.security.auth.Subject;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public interface EjbDeployment extends Interceptor {
    /**
     * Unique id used for locating the deployment
     * No assumptions are made about the type of
     * this object other than it can hash uniquely
     * @return the id of the deployment
     */
    String getContainerId();

    /**
     * Return the name of the EJB
     * @return the name of the EJB
     */
    String getEjbName();

    int getMethodIndex(Method method);

    ClassLoader getClassLoader();

    EjbDeployment getUnmanagedReference();

    InterfaceMethodSignature[] getSignatures();

    /**
     * Returns the subject to use if the client is not authenticated.
     * <p/>
     * This subject must have been registered at deployment startup, to properly
     * work with the deployment's interceptors.
     * @return the default subject
     * @see org.apache.geronimo.security.ContextManager#registerSubject(javax.security.auth.Subject)
     */
    Subject getDefaultSubject();
}
