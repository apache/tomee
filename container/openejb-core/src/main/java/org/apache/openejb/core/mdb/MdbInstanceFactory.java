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

package org.apache.openejb.core.mdb;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.TimerServiceWrapper;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.EJBContext;
import jakarta.ejb.MessageDrivenBean;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.resource.spi.UnavailableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A MdbInstanceFactory creates instances of message driven beans for a single instance. This class differs from other
 * instance managers in OpenEJB as it doesn't do pooling and it creates instances for only a single EJB deployment.
 * </p>
 * The MdbContainer assumes that the resouce adapter is pooling message endpoints so a second level of pooling in the
 * container would be inefficient.  This is true of all known resouce adapters in opensource (ActiveMQ), so if this is
 * a poor assumption for your resource adapter, contact the OpenEJB developers.
 * </p>
 * This class can optionally limit the number of bean instances and therefore the message endpoints available to the
 * resource adapter.
 */
public class MdbInstanceFactory {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final BeanContext beanContext;
    private final int instanceLimit;
    private int instanceCount;
    private final MdbContext mdbContext;

    /**
     * Creates a MdbInstanceFactory for a single specific deployment.
     *
     * @param beanContext     the deployment for which instances will be created
     * @param securityService the transaction manager for this container system
     * @param instanceLimit   the maximal number of instances or <= 0 if unlimited
     */
    public MdbInstanceFactory(final BeanContext beanContext, final SecurityService securityService, final int instanceLimit) throws OpenEJBException {
        this.beanContext = beanContext;
        this.instanceLimit = instanceLimit;
        mdbContext = new MdbContext(securityService);

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", mdbContext);
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (final NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/TimerService", e);
        }

        beanContext.set(EJBContext.class, this.mdbContext);
    }

    /**
     * Gets the maximal number of instances that can exist at any time.
     *
     * @return the maximum number of instances or <= 0 if unlimitied
     */
    public int getInstanceLimit() {
        return instanceLimit;
    }

    /**
     * Gets the current number of created instances.
     *
     * @return the current number of instances created
     */
    public synchronized int getInstanceCount() {
        return instanceCount;
    }

    /**
     * Creates a new mdb instance preforming all necessary lifecycle callbacks
     *
     * @param ignoreInstanceCount
     * @return a new message driven bean instance
     * @throws UnavailableException if the instance limit has been exceeded or
     *                              if an exception occurs while creating the bean instance
     */
    public Object createInstance(final boolean ignoreInstanceCount) throws UnavailableException {
        if (!ignoreInstanceCount) {
            synchronized (this) {
                // check the instance limit
                if (instanceLimit > 0 && instanceCount >= instanceLimit) {
                    throw new UnavailableException("Only " + instanceLimit + " instances can be created");
                }
                // increment the instance count
                instanceCount++;
            }
        }

        try {
            final Object bean = constructBean();
            return bean;
        } catch (final UnavailableException e) {
            // decrement the instance count
            if (!ignoreInstanceCount) {
                synchronized (this) {
                    instanceCount--;
                }
            }

            throw e;
        }
    }

    /**
     * Frees an instance no longer needed by the resource adapter.  This method makes all the necessary lifecycle
     * callbacks and decrements the instance count.  This method should not be used to disposed of beans that have
     * thrown a system exception.  Instead the discardInstance method should be called.
     *
     * @param instance             the bean instance to free
     * @param ignoredInstanceCount
     */
    public void freeInstance(final Instance instance, final boolean ignoredInstanceCount) {
        if (instance == null) {
            throw new NullPointerException("bean is null");
        }

        // decrement the instance count
        if (!ignoredInstanceCount) {
            synchronized (this) {
                instanceCount--;
            }
        }

        final ThreadContext callContext = ThreadContext.getThreadContext();

        final Operation originalOperation = callContext == null ? null : callContext.getCurrentOperation();
        final BaseContext.State[] originalAllowedStates = callContext == null ? null : callContext.getCurrentAllowedStates();

        try {
            // call post destroy method
            if (callContext != null) {
                callContext.setCurrentOperation(Operation.PRE_DESTROY);
            }
            final Method remove = instance.bean instanceof MessageDrivenBean ? MessageDrivenBean.class.getMethod("ejbRemove") : null;
            final List<InterceptorData> callbackInterceptors = beanContext.getCallbackInterceptors();
            final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);
            interceptorStack.invoke();
            if (instance.creationalContext != null) {
                instance.creationalContext.release();
            }
        } catch (final Throwable re) {
            MdbInstanceFactory.logger.error("The bean instance " + instance.bean + " threw a system exception:" + re, re);
        } finally {

            if (callContext != null) {
                callContext.setCurrentOperation(originalOperation);
                callContext.setCurrentAllowedStates(originalAllowedStates);
            }
        }
    }

    /**
     * Recreates a bean instance that has thrown a system exception.  As required by the EJB specification, lifecycle
     * callbacks are not invoked.  To normally free a bean instance call the freeInstance method.
     *
     * @param bean the bean instance to discard
     * @return the new replacement bean instance
     */
    public Object recreateInstance(final Object bean) throws UnavailableException {
        if (bean == null) {
            throw new NullPointerException("bean is null");
        }
        final Object newBean = constructBean();
        return newBean;
    }

    private Object constructBean() throws UnavailableException {
        final BeanContext beanContext = this.beanContext;

        final ThreadContext callContext = new ThreadContext(beanContext, null, Operation.INJECTION);
        final ThreadContext oldContext = ThreadContext.enter(callContext);

        try {
            final InstanceContext context = beanContext.newInstance();

            if (context.getBean() instanceof MessageDrivenBean) {
                callContext.setCurrentOperation(Operation.CREATE);
                final Method create = beanContext.getCreateMethod();
                final InterceptorStack ejbCreate = new InterceptorStack(context.getBean(), create, Operation.CREATE, new ArrayList(), new HashMap());
                ejbCreate.invoke();
            }

            return new Instance(context.getBean(), context.getInterceptors(), context.getCreationalContext());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            final String message = "The bean instance threw a system exception:" + e;
            MdbInstanceFactory.logger.error(message, e);
            throw new UnavailableException(message, e);
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

}
