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
import java.util.Collection;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class MethodContext {
    private final BeanContext beanContext;
    private final Method beanMethod;
    private final List<ScheduleData> schedules = new ArrayList<ScheduleData>();
    private final List<InterceptorData> methodInterceptors = new ArrayList<InterceptorData>();
    private LockType methodConcurrencyAttribute;
    private TransactionType transactionType;
    private Duration accessTimeout;

    /**
     * Only initialized if this method represents metadata
     * associated with a specific interface view and not the
     * bean method itself.
     */
    private MethodContext beanMethodContext;

    public MethodContext(BeanContext beanContext, Method beanMethod) {
        this.beanContext = beanContext;
        this.beanMethod = beanMethod;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Method getBeanMethod() {
        return beanMethod;
    }

    public List<InterceptorData> getInterceptors() {
        return methodInterceptors;
    }

    public LockType getMethodConcurrencyAttribute() {
        return methodConcurrencyAttribute;
    }

    public void setMethodConcurrencyAttribute(LockType methodConcurrencyAttribute) {
        this.methodConcurrencyAttribute = methodConcurrencyAttribute;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }


    public static class InterfaceMethodContext {
        private final MethodContext beanMethod;
        private TransactionType transactionType;

        public InterfaceMethodContext(MethodContext beanMethod) {
            this.beanMethod = beanMethod;
        }

        public void setTransactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
        }

        public TransactionType getTransactionType() {
            return transactionType != null ? transactionType : beanMethod.getTransactionType();
        }
    }
}
