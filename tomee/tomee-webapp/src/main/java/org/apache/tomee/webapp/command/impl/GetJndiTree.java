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

package org.apache.tomee.webapp.command.impl;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.proxy.LocalBeanProxyGeneratorImpl;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;
import org.apache.tomee.webapp.listener.UserSessionListener;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.util.*;

public class GetJndiTree implements Command {

    private String getBeanType(Object bean) {
        if (bean instanceof Context) {
            return "CONTEXT";

        } else if (bean instanceof java.rmi.Remote
                || bean instanceof org.apache.openejb.core.ivm.IntraVmProxy
                || (bean != null && LocalBeanProxyGeneratorImpl.isLocalBean(bean.getClass()))) {
            return "BEAN";
        } else {
            return "OTHER";
        }
    }

    private Map<String, Object> buildNode(String parentCtxPath, NameClassPair pair, Context ctx) throws NamingException {
        final Map<String, Object> node = new HashMap<String, Object>();
        node.put("ctxPath", parentCtxPath);

        final String name = pair.getName();
        node.put("name", name);

        final Object obj = ctx.lookup(name);
        final String beanType = getBeanType(obj);
        node.put("type", beanType);
        if ("CONTEXT".equals(beanType)) {
            node.put("children", Collections.emptyList());
        }

        return node;
    }

    private Context getContext(Context ctx, List<String> path) throws NamingException {
        if (path.isEmpty()) {
            return ctx;
        }

        String name = path.remove(0);
        final Object obj = ctx.lookup(name);

        if (obj instanceof Context) {
            return getContext((Context) obj, path);

        } else {
            throw new IllegalStateException("obj should be an instance of Context");
        }
    }

    private void buildNames(String parentCtxPath, Context ctx, Map<String, Object> json) throws NamingException {
        final List<Map<String, Object>> objs = new ArrayList<Map<String, Object>>();
        json.put("names", objs);

        final NamingEnumeration<NameClassPair> namingEnumeration = ctx.list("");
        if (namingEnumeration != null) {
            while (namingEnumeration.hasMoreElements()) {
                objs.add(buildNode(parentCtxPath, namingEnumeration.next(), ctx));
            }
        }
    }

    private void buildClass(Context ctx, String name, Map<String, Object> json) throws NamingException {
        final Map<String, Object> values = new HashMap<String, Object>();
        json.put("cls", values);

        final Object obj = ctx.lookup(name);
        values.put("type", getBeanType(obj));

        Class<?> cls;
        {
            try {
                final BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(obj);
                final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                final BeanContext ejb = containerSystem.getBeanContext(handler.deploymentID);

                values.put("componentType", ejb.getComponentType().name());

                cls = ejb.getBeanClass();

            } catch (Exception e) {
                cls = obj.getClass();

            }

            values.put("beanClass", cls.getCanonicalName());

            final List<String> interfaces = new ArrayList<String>();
            values.put("interfaces", interfaces);

            for (Class<?> myInterface : cls.getInterfaces()) {
                interfaces.add(myInterface.getCanonicalName());
            }
        }

        {
            final List<Map<String, Object>> methods = new ArrayList<Map<String, Object>>();
            values.put("methods", methods);

            for (Method method : cls.getDeclaredMethods()) {
                final Map<String, Object> methodMap = new HashMap<String, Object>();
                methods.add(methodMap);

                final Class<?> returnType = method.getReturnType();
                if (returnType != null) {
                    methodMap.put("returns", returnType.getCanonicalName());
                }
                methodMap.put("name", method.getName());

                final List<String> parameterTypes = new ArrayList<String>();
                methodMap.put("parameters", parameterTypes);

                for (Class<?> parameter : method.getParameterTypes()) {
                    parameterTypes.add(parameter.getCanonicalName());

                }
            }
        }
    }

    @Override
    public Object execute(Params params) throws Exception {
        final Context initCtx = UserSessionListener.getServiceContext(params.getReq().getSession()).getUserContext();

        final Context ctx;
        final String strPath = params.getString("path");
        if (strPath == null) {
            ctx = initCtx;
        } else {
            final List<String> path = new ArrayList<String>();
            path.addAll(Arrays.asList(params.getString("path").split(",")));
            ctx = getContext(initCtx, path);
        }

        final Map<String, Object> json = new HashMap<String, Object>();
        final String name = params.getString("name");
        if (name == null) {
            buildNames(strPath, ctx, json);

        } else {
            buildClass(ctx, name, json);
        }
        return json;
    }
}
