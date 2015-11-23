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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.InvalidateReferenceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Level;

public class PojoInvoker extends JAXRSInvoker {
    protected Object performInvocation(final Exchange exchange, final Object serviceObject,
                                       final Method m, final Object[] paramArray) throws Exception {
        final Object[] args = insertExchange(m, paramArray, exchange);
        final ClassLoader tcclToUse = getClassLoader(exchange);
        final Thread thread = Thread.currentThread();
        ClassLoader oldLoader = null;
        if (tcclToUse != null) {
            oldLoader = thread.getContextClassLoader();
            thread.setContextClassLoader(tcclToUse);
        }
        try {
            return m.invoke(serviceObject, args);
        } finally {
            if (tcclToUse != null) {
                thread.setContextClassLoader(oldLoader);
            }
        }
    }

    private ClassLoader getClassLoader(final Exchange exchange) {
        final Message inMessage = exchange.getInMessage();
        if (inMessage == null) {
            return null;
        }
        final OpenEJBPerRequestPojoResourceProvider requestPojoResourceProvider = inMessage.get(OpenEJBPerRequestPojoResourceProvider.class);
        if (requestPojoResourceProvider != null) {
            return requestPojoResourceProvider.getClassLoader();
        }
        return null;
    }
}
