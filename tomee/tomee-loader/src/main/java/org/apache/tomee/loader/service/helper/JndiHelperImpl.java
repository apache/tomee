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

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.tomee.loader.service.ServiceContext;
import org.apache.tomee.loader.service.ServiceException;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JndiHelperImpl implements JndiHelper {

    private final ServiceContext srvCtx;

    public JndiHelperImpl(ServiceContext srvCtx) {
        this.srvCtx = srvCtx;
    }

    @Override
    public List<Map<String, Object>> getJndi(String path) {
        final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        try {
            mountJndiList(result, this.srvCtx.getContext(), path);
        } catch (NamingException e) {
            //Throwing a runtimeexception instead.
            throw new ServiceException(e);
        }

        return result;
    }

    private void mountJndiList(List<Map<String, Object>> jndi, Context context, String root) throws NamingException {
        final NamingEnumeration namingEnumeration;
        try {
            namingEnumeration = context.list(root);
        } catch (NamingException e) {
            //not found?
            return;
        }
        while (namingEnumeration.hasMoreElements()) {
            final NameClassPair pair = (NameClassPair) namingEnumeration.next();
            final String key = pair.getName();

            System.out.println("(A)");

            final Object obj;
            try {
                obj = context.lookup(key);
            } catch (NamingException e) {
                //not found?
                continue;
            }

            if (Context.class.isInstance(obj)) {
                mountJndiList(jndi, Context.class.cast(obj), key);
            } else {
                final Map<String, Object> dto = new HashMap<String, Object>();
                dto.put("path", key);
                dto.put("name", pair.getName());
                dto.put("value", getStr(obj));

                jndi.add(dto);
            }
        }
    }

    private void populateClassList(List<String> list, List<Class> classes) {
        if (classes == null) {
            return;
        }
        for (Class<?> cls : classes) {
            list.add(getStr(cls));
        }
    }

    private BeanContext getDeployment(String deploymentID) {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        BeanContext ejb = containerSystem.getBeanContext(deploymentID);
        return ejb;
    }

    private String getDeploymentId(Object ejbObj) throws NamingException {
        final BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(ejbObj);
        return getStr(handler.deploymentID);
    }

    private String getStr(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
