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

package org.apache.tomee.loader.service.helper;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OpenEJBHelperImpl implements OpenEJBHelper {
    private final ContainerSystem container = SystemInstance.get().getComponent(ContainerSystem.class);

    @Override
    public List<AppContext> getAppContexts() {
        return container.getAppContexts();
    }

    @Override
    public Object lookup(String path) throws NamingException {
        return container.getJNDIContext().lookup(path);
    }

    public AppContext app(final String name) {
        for (AppContext appContext : getAppContexts()) {
            final String appName = appContext.getId();
            if (appName.equals(name)) {
                return appContext;
            }
        }
        return null;
    }

    public BeanContext bean(final String app, final String name) {
        AppContext appCtx = app(app);
        if (appCtx == null) {
            return null;
        }
        for (BeanContext ctx : appCtx.getBeanContexts()) {
            if (ctx.getDeploymentID().equals(name)) {
                return ctx;
            }
        }
        return null;
    }

    public Method method(final String app, final String name, final long id) {
        final BeanContext bean = bean(app, name);
        if (bean != null) {
            final Collection<MethodInfo> methods = methods(baseClass(bean));
            for (MethodInfo method : methods) {
                if (method.getId() == id) {
                    return method.getMethod();
                }
            }
        }
        return null;
    }

    public Class<?> baseClass(BeanContext beanContext) {
        if (beanContext.isLocalbean()) {
            return beanContext.getBeanClass();
        } else if (beanContext.getBusinessLocalInterfaces().size() > 0) {
            return beanContext.getBusinessLocalInterface();
        } else if (beanContext.getBusinessRemoteInterface() != null) {
            return beanContext.getBusinessRemoteInterface();
        }
        return beanContext.getBeanClass();
    }

    public List<MethodInfo> methods(Class<?> beanClass) {
        final List<MethodInfo> methods = new ArrayList<MethodInfo>();
        Class<?> current = beanClass;
        do {
            for (Method method : current.getDeclaredMethods()) {
                methods.add(new MethodInfo(method.toGenericString()
                        .replace(beanClass.getName().concat("."), "")
                        .replace("java.lang.", ""),
                        method.hashCode(), method));
            }
            current = current.getSuperclass();
        } while (current != null && !current.equals(Object.class));
        return methods;
    }



    public static class MethodInfo {
        private String signature;
        private long id;
        private Method method;

        public MethodInfo(String signature, long id, Method mtd) {
            this.signature = signature;
            this.id = id;
            this.method = mtd;
        }

        public String getSignature() {
            return signature;
        }

        public long getId() {
            return id;
        }

        public Method getMethod() {
            return method;
        }
    }
}
