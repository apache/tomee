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
package org.apache.tomee.catalina.cdi;

import org.apache.catalina.connector.Request;
import org.apache.openejb.AppContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.tomee.catalina.OpenEJBSecurityListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServletContextHandler implements InvocationHandler {
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // ITE are handler by Proxys
        final Request request = OpenEJBSecurityListener.requests.get();
        if (request != null) {
            return method.invoke(request.getServletContext(), args);
        }

        OpenEJBSecurityListener.requests.remove(); // can be a not container thread so clean it up
        for (final AppContext a : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
            for (final WebContext w : a.getWebContexts()) {
                if (w.getClassLoader() == Thread.currentThread().getContextClassLoader()) { // not in CXF so == should be fine
                    return method.invoke(w.getServletContext(), args);
                }
            }
        }

        throw new IllegalStateException("Didnt find a web context for " + Thread.currentThread().getContextClassLoader());
    }
}
