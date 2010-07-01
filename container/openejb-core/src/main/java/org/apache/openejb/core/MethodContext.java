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
package org.apache.openejb.core;

import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.util.Duration;

import javax.ejb.LockType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class MethodContext {
    private final CoreDeploymentInfo beanContext;
    private final Method beanMethod;
    private final List<ScheduleData> schedules = new ArrayList<ScheduleData>();
    private final List<InterceptorData> interceptors = new ArrayList<InterceptorData>();
    private LockType lockType;
    private TransactionType transactionType;
    private Duration accessTimeout;

    public MethodContext(CoreDeploymentInfo beanContext, Method beanMethod) {
        this.beanContext = beanContext;
        this.beanMethod = beanMethod;
    }

    public void setAccessTimeout(Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getAccessTimeout() {
        return accessTimeout != null? accessTimeout: beanContext.getAccessTimeout();
    }

    public CoreDeploymentInfo getBeanContext() {
        return beanContext;
    }

    public Method getBeanMethod() {
        return beanMethod;
    }

    public void setInterceptors(List<InterceptorData> interceptors) {
        this.interceptors.clear();
        this.interceptors.addAll(interceptors);
    }

    public List<InterceptorData> getInterceptors() {
        List<InterceptorData> datas = beanContext.getInterceptorData();
        datas.addAll(interceptors);
        return datas;
    }

    public LockType getLockType() {
        return lockType != null? lockType: beanContext.getLockType();
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public TransactionType getTransactionType() {
        return transactionType != null? transactionType: beanContext.getTransactionType();
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<ScheduleData> getSchedules() {
        return schedules;
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

        public InterfaceMethodContext(MethodContext beanMethod, Method method) {
            this.beanMethod = beanMethod;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public void setTransactionType(TransactionType transactionType) {
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
