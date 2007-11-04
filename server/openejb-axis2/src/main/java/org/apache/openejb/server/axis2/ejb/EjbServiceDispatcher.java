/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.server.axis2.ejb;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.server.dispatcher.JavaBeanDispatcher;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

public class EjbServiceDispatcher extends JavaBeanDispatcher {
    private InvocationContext invContext;

    public EjbServiceDispatcher(Class serviceImplClass, InvocationContext invContext) {
        super(serviceImplClass, null);
        this.invContext = invContext;
    }

    protected Object invokeService(MessageContext ctx, Method method, Object obj, Object args[]) throws Exception {
        this.invContext.setParameters(args);
        return this.invContext.proceed();
    }
}
