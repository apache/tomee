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

import org.apache.catalina.connector.Request;
import org.apache.openejb.AppContext;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.apache.tomee.catalina.TomcatWebAppBuilder.CONTEXTS;

public interface ServletContextProxy extends ServletContext, Serializable {

    public Object writeReplace() throws ObjectStreamException;

    public static ServletContext get() {
        return (ServletContext) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { ServletContextProxy.class, ServletContext.class, CdiAppContextsService.FiredManually.class, Serializable.class },
                new Handler());
    }

    public static class Handler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("writeReplace") && method.getParameterTypes().length == 0) {
                return new Serialized();
            }

            // ITE are handled by Proxys
            final Request request = OpenEJBSecurityListener.requests.get();
            if (request != null) {
                return method.invoke(request.getServletContext(), args);
            }

            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            final ServletContext c = CONTEXTS.get(contextClassLoader);
            if (c != null) {
                return method.invoke(c, args);
            }

            OpenEJBSecurityListener.requests.remove(); // can be a not container thread so clean it up
            for (final AppContext a : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
                for (final WebContext w : a.getWebContexts()) {
                    if (w.getClassLoader() == contextClassLoader) { // not in CXF so == should be fine
                        return method.invoke(w.getServletContext(), args);
                    }
                }
            }

            throw new IllegalStateException("Didnt find a web context for " + contextClassLoader);
        }
    }

    public static class Serialized implements Serializable {
        public Object readResolve() throws ObjectStreamException {
            return ServletContextProxy.get();
        }
    }
}
