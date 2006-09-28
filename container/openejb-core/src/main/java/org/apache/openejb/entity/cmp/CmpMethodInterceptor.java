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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @version $Revision$ $Date$
 */
final class CmpMethodInterceptor implements MethodInterceptor {
    private final InstanceOperation[] itable;
    private CmpInstanceContext instanceContext;

    public CmpMethodInterceptor(InstanceOperation[] itable) {
        this.itable = itable;
    }

    public void setInstanceContext(CmpInstanceContext instanceContext) {
        if (this.instanceContext != null) throw new IllegalStateException("instanceContext already set");
        if (instanceContext == null) throw new NullPointerException("instance context is null");
        this.instanceContext = instanceContext;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        int index = methodProxy.getSuperIndex();
        InstanceOperation iop = itable[index];
        return iop.invokeInstance(instanceContext, objects);
    }
}
