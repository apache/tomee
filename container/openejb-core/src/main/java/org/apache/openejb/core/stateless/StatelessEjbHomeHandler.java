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

package org.apache.openejb.core.stateless;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;

import jakarta.ejb.RemoveException;
import java.lang.reflect.Method;
import java.util.List;

public class StatelessEjbHomeHandler extends EjbHomeProxyHandler {

    public StatelessEjbHomeHandler(final BeanContext beanContext, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, interfaceType, interfaces, mainInterface);
    }

    @Override
    protected Object findX(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    @Override
    protected Object removeByPrimaryKey(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
    }

    @Override
    protected Object removeWithHandle(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        // stateless can't be removed
        return null;
    }

    @Override
    protected EjbObjectProxyHandler newEjbObjectHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        return new StatelessEjbObjectHandler(getBeanContext(), pk, interfaceType, interfaces, mainInterface);
    }

}
