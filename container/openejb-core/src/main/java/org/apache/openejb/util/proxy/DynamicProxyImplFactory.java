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

package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * @author rmannibucau
 */
public class DynamicProxyImplFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, BeanContext.class);

    public static Object newProxy(BeanContext context, java.lang.reflect.InvocationHandler invocationHandler) {
        if (invocationHandler instanceof QueryProxy) {
            String emLookupName = context.getInjections().get(context.getInjections().size() - 1).getJndiName();
            EntityManager em;
            try {
                em = (EntityManager) context.getJndiEnc().lookup(emLookupName);
            } catch (NamingException e) {
                throw new RuntimeException("a dynamic bean should reference at least one correct PersistenceContext", e);
            }

            ((QueryProxy) invocationHandler).setEntityManager(em);
        }

        try {
            return ProxyManager.newProxyInstance(context.getLocalInterface(), new Handler(invocationHandler));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("illegal access", e);
        }
    }

    private static class Handler implements InvocationHandler {
        private java.lang.reflect.InvocationHandler handler;

        private Handler(java.lang.reflect.InvocationHandler handler) {
            this.handler = handler;
        }

        @Override public InvocationHandler getInvocationHandler() {
            return this;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return handler.invoke(proxy, method, args);
        }
    }
}
