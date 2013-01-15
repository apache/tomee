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
package org.apache.tomee.catalina.cdi;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.context.creational.BeanInstanceBag;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

public class SessionNormalScopeBeanHandler extends NormalScopedBeanInterceptorHandler {
    public SessionNormalScopeBeanHandler(final OwbBean<?> bean, final CreationalContext<?> creationalContext) {
        super(bean, creationalContext);
    }

    @Override
    public Object invoke(final Object instance, final Method method, final Object[] arguments) throws Throwable {
        final Object webbeansInstance = getContextualInstance();
        try {
            return super.invoke(webbeansInstance, method, arguments, (CreationalContextImpl<?>) getContextualCreationalContext());
        } finally {
            updateBean(webbeansInstance);
        }
    }

    private void updateBean(final Object value) {
        final HttpSession session = session();
        if (session == null) {
            return;
        }

        // go through all listeners to be able to be replicated or do any processing which can be done
        final String key = SessionContextBackedByHttpSession.key(bean);
        final BeanInstanceBag<Object> bag = (BeanInstanceBag<Object>) session.getAttribute(key);
        if (bag != null) {
            bag.setBeanInstance(value);
            session.setAttribute(key, bag);
        }
    }

    private HttpSession session() {
        final Context context = getBeanManager().getContext(SessionScoped.class);
        if (!SessionContextBackedByHttpSession.class.isInstance(context)) {
            return null;
        }
        return ((SessionContextBackedByHttpSession) context).getSession();
    }
}
