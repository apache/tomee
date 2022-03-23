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

package org.apache.openejb;

import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.util.Duration;

import jakarta.ejb.LockType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class MethodContext {
    private final BeanContext beanContext;
    private final Method beanMethod;
    private final List<ScheduleData> schedules = new ArrayList<>();
    private final List<InterceptorData> interceptors = new ArrayList<>();
    private final Set<InterceptorData> cdiInterceptors = new LinkedHashSet<>();
    private InterceptorData self = null;
    private LockType lockType;
    private TransactionType transactionType;
    private Duration accessTimeout;
    private boolean asynchronous;

    public MethodContext(final BeanContext beanContext, final Method beanMethod) {
        this.beanContext = beanContext;
        this.beanMethod = beanMethod;
    }

    public void setSelfInterception(final InterceptorData data) {
        self = data;
    }

    public void setAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Method getBeanMethod() {
        return beanMethod;
    }

    public void addCdiInterceptor(final InterceptorData data) {
        cdiInterceptors.add(data);
    }

    public void setInterceptors(final List<InterceptorData> interceptors) {
        this.interceptors.clear();
        this.interceptors.addAll(interceptors);
    }

    public List<InterceptorData> getInterceptors() {
        final List<InterceptorData> datas = beanContext.getInterceptorData();
        datas.addAll(interceptors);
        datas.addAll(beanContext.getCdiInterceptors());
        datas.addAll(cdiInterceptors);
        if (self != null) {
            datas.add(self); // always last, that's why putting it in interceptors doesn't work
        }
        return datas;
    }

    public LockType getLockType() {
        return lockType != null ? lockType : beanContext.getLockType();
    }

    public void setLockType(final LockType lockType) {
        this.lockType = lockType;
    }

    public TransactionType getTransactionType() {
        return transactionType != null ? transactionType : beanContext.getTransactionType();
    }

    public void setTransactionType(final TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<ScheduleData> getSchedules() {
        return schedules;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(final boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    /**
     * Currently (and as a matter of legacy) only EJB 2.x style
     * interfaces may have different transaction attributes for an
     * individual interface method.
     */
    public static class InterfaceMethodContext {
        private final MethodContext beanMethod;
        private final Method method;
        private TransactionType transactionType;

        public InterfaceMethodContext(final MethodContext beanMethod, final Method method) {
            this.beanMethod = beanMethod;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public void setTransactionType(final TransactionType transactionType) {
            this.transactionType = transactionType;
        }

        public TransactionType getTransactionType() {
            return transactionType != null ? transactionType : beanMethod.getTransactionType();
        }

        public MethodContext getBeanMethod() {
            return beanMethod;
        }
    }
}
