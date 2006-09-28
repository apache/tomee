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
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.entity.EntityInstanceContext;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.TimerState;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BmpCreateMethod implements VirtualOperation, Serializable {
    private final Class beanClass;
    private final MethodSignature createSignature;
    private final MethodSignature postCreateSignature;
    private final transient FastClass fastBeanClass;
    private final transient int createIndex;
    private final transient int postCreateIndex;


    public BmpCreateMethod(
            Class beanClass,
            MethodSignature createSignature,
            MethodSignature postCreateSignature) {

        this.beanClass = beanClass;
        this.createSignature = createSignature;
        this.postCreateSignature = postCreateSignature;

        fastBeanClass = FastClass.create(beanClass);
        Method createMethod = createSignature.getMethod(beanClass);
        if (createMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + createSignature);
        }
        createIndex = fastBeanClass.getIndex(createMethod.getName(), createMethod.getParameterTypes());

        Method postCreateMethod = postCreateSignature.getMethod(beanClass);
        if (postCreateMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement post create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + postCreateSignature);
        }
        postCreateIndex = fastBeanClass.getIndex(postCreateMethod.getName(), postCreateMethod.getParameterTypes());
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        EntityInstanceContext ctx = (EntityInstanceContext) invocation.getEJBInstanceContext();

        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();

        // call the create method
        Object id;
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBCREATE);
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            id = fastBeanClass.invoke(createIndex, instance, args);
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

        // assign the context the new id
        ctx.setId(id);

        // associate the new BMP instance with the tx cache
        ctx.setLoaded(true);
        invocation.getEjbTransactionData().associate(ctx);

        // call the post create method
        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            ctx.setTimerState(EJBOperation.EJBPOSTCREATE);
            fastBeanClass.invoke(postCreateIndex, instance, args);
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


        EJBInterfaceType type = invocation.getType();
        return invocation.createResult(getReference(type.isLocal(), ctx.getProxyFactory(), id));
    }

    private Object getReference(boolean local, EJBProxyFactory proxyFactory, Object id) {
        if (local) {
            return proxyFactory.getEJBLocalObject(id);
        } else {
            return proxyFactory.getEJBObject(id);
        }
    }

    private Object readResolve() {
        return new BmpCreateMethod(beanClass, createSignature, postCreateSignature);
    }
}
