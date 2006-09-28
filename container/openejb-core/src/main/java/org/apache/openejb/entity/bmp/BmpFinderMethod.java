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
package org.apache.openejb.entity.bmp;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.TimerState;
import org.apache.openejb.util.SerializableEnumeration;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BmpFinderMethod implements VirtualOperation, Serializable  {
    private final Class beanClass;
    private final MethodSignature finderSignature;

    private transient FastClass fastClass;
    private transient int finderIndex;

    public BmpFinderMethod(Class beanClass, MethodSignature finderSignature) {
        this.beanClass = beanClass;
        this.finderSignature = finderSignature;

        fastClass = FastClass.create(beanClass);
        Method javaMethod = finderSignature.getMethod(beanClass);
        if(javaMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement finder method:" +
                    " beanClass=" + beanClass.getName() + " method=" + finderSignature);
        }
        finderIndex = fastClass.getIndex(javaMethod.getName(), javaMethod.getParameterTypes());
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();

        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        Object finderResult;
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBFIND);
        try {
            ctx.setOperation(EJBOperation.EJBFIND);
            finderResult = fastClass.invoke(finderIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        boolean local = invocation.getType().isLocal();
        EJBProxyFactory proxyFactory = ctx.getProxyFactory();

        if (finderResult instanceof Enumeration) {
            Enumeration e = (Enumeration) finderResult;
            ArrayList values = new ArrayList();
            while (e.hasMoreElements()) {
                values.add(getReference(local, proxyFactory, e.nextElement()));
            }
            return invocation.createResult(new SerializableEnumeration(values.toArray()));
        } else if (finderResult instanceof Collection) {
            Collection c = (Collection) finderResult;
            ArrayList result = new ArrayList(c.size());
            for (Iterator i = c.iterator(); i.hasNext();) {
                result.add(getReference(local, proxyFactory, i.next()));
            }
            return invocation.createResult(result);
        } else {
            return invocation.createResult(getReference(local, proxyFactory, finderResult));
        }
    }

    private Object getReference(boolean local, EJBProxyFactory proxyFactory, Object id) {
        if (id == null) {
            // yes, finders can return null
            return null;
        } else if (local) {
            return proxyFactory.getEJBLocalObject(id);
        } else {
            return proxyFactory.getEJBObject(id);
        }
    }

    private Object readResolve() {
        return new BmpFinderMethod(beanClass, finderSignature);
    }
}
