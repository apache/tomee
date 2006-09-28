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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.TimerState;

/**
 * @version $Revision$ $Date$
 */
public class CmpCreateMethod implements VirtualOperation, Serializable {
    private static final long serialVersionUID = -1846351514946502346L;
    private final Class beanClass;
    private final Cmp1Bridge cmp1Bridge;
    private final MethodSignature createSignature;
    private final MethodSignature postCreateSignature;

    private final EjbCmpEngine ejbCmpEngine;

    private final transient FastClass fastBeanClass;
    private final transient int createIndex;
    private final transient int postCreateIndex;

    public CmpCreateMethod(Class beanClass,
            Cmp1Bridge cmp1Bridge,
            MethodSignature createSignature,
            MethodSignature postCreateSignature,
            EjbCmpEngine ejbCmpEngine) {

        this.beanClass = beanClass;
        this.cmp1Bridge = cmp1Bridge;
        this.createSignature = createSignature;
        this.postCreateSignature = postCreateSignature;
        this.ejbCmpEngine = ejbCmpEngine;

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
        CmpInstanceContext ctx = (CmpInstanceContext) invocation.getEJBInstanceContext();

        ejbCmpEngine.beforeCreate(ctx);

        // call the create method
        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBCREATE);
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            fastBeanClass.invoke(createIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception) t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        if (cmp1Bridge != null) {
            // load all of the cmp1 instance fields into the cmp engine
            cmp1Bridge.copyFromObjectToCmp(ctx);
        }

        // create the new instance using the data set during the ejbCreate callback
        EjbTransactionContext ejbTransactionContext = invocation.getEjbTransactionData();
        try {
            ejbCmpEngine.afterCreate(ctx, ejbTransactionContext);
        } catch (DuplicateKeyException e) {
            return invocation.createExceptionResult(e);
        }

        // associate the new cmp instance with the tx context
        ctx.setLoaded(true);
        ejbTransactionContext.associate(ctx);

        // call the post create method
        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            ctx.setTimerState(EJBOperation.EJBPOSTCREATE);
            fastBeanClass.invoke(postCreateIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                // we do not force rollback, that is up to the application
                return invocation.createExceptionResult((Exception) t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        // return a new proxy
        Object id = ctx.getId();
        Object ejbProxy = getEjbProxy(invocation.getType().isLocal(), ctx.getProxyFactory(), id);
        return invocation.createResult(ejbProxy);
    }

    private Object getEjbProxy(boolean local, EJBProxyFactory proxyFactory, Object id) {
        if (local) {
            return proxyFactory.getEJBLocalObject(id);
        } else {
            return proxyFactory.getEJBObject(id);
        }
    }

    protected Object readResolve() {
        return new CmpCreateMethod(beanClass, cmp1Bridge, createSignature, postCreateSignature, ejbCmpEngine);
    }
}
