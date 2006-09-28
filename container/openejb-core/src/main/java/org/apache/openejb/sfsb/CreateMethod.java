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
package org.apache.openejb.sfsb;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ejb.SessionBean;

import org.apache.geronimo.interceptor.InvocationResult;

import net.sf.cglib.reflect.FastClass;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;

/**
 * @version $Revision$ $Date$
 */
public class CreateMethod implements VirtualOperation, Serializable {
    private final Class beanClass;
    private final MethodSignature createSignature;

    private final transient FastClass fastClass;
    private final transient int createIndex;

    public CreateMethod(Class beanClass, MethodSignature signature) {
        this.beanClass = beanClass;
        this.createSignature = signature;

        fastClass = FastClass.create(beanClass);
        Method javaMethod = signature.getMethod(beanClass);
        if(javaMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + signature);
        }
        createIndex = fastClass.getIndex(javaMethod.getName(), javaMethod.getParameterTypes());
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();

        // call create
        SessionBean instance = (SessionBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            fastClass.invoke(createIndex, instance, args);
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
        }

        // return a ref
        EJBInterfaceType type = invocation.getType();
        return invocation.createResult(getReference(type.isLocal(), ctx, ctx.getId()));
    }

    private Object getReference(boolean local, EJBInstanceContext ctx, Object id) {
        if (local) {
            return ctx.getProxyFactory().getEJBLocalObject(id);
        } else {
            return ctx.getProxyFactory().getEJBObject(id);
        }
    }

    private Object readResolve() {
        return new CreateMethod(beanClass, createSignature);
    }
}
