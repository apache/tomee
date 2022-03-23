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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.MethodContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.LockType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class MethodConcurrencyBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodConcurrencyBuilder.class);

    public void build(final HashMap<String, BeanContext> deployments, final List<MethodConcurrencyInfo> methodConcurrencys) throws OpenEJBException {
        for (final BeanContext beanContext : deployments.values()) {
            MethodConcurrencyBuilder.applyConcurrencyAttributes(beanContext, methodConcurrencys);
        }
    }

    public static void applyConcurrencyAttributes(final BeanContext beanContext, final List<MethodConcurrencyInfo> methodConcurrencyInfos) throws OpenEJBException {

        if (beanContext.isBeanManagedConcurrency()) {
            return;
        }

        final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), MethodConcurrencyBuilder.class);

        final List<MethodConcurrencyInfo> lockInfos = new ArrayList<>();
        final List<MethodConcurrencyInfo> accessTimeoutInfos = new ArrayList<>();

        MethodConcurrencyBuilder.normalize(methodConcurrencyInfos, lockInfos, accessTimeoutInfos);

        Map<Method, MethodAttributeInfo> attributes;

        // handle @Lock
        attributes = MethodInfoUtil.resolveAttributes(lockInfos, beanContext);

        if (log.isDebugEnabled()) {
            for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                final Method method = entry.getKey();
                final MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
                log.debug("Lock: " + method + " -- " + MethodInfoUtil.toString(value.methods.get(0)) +
                    " " + value.concurrencyAttribute);
            }
        }

        for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
            final MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
            final MethodContext methodContext = beanContext.getMethodContext(entry.getKey());
            final String s = value.concurrencyAttribute.toUpperCase();
            methodContext.setLockType(LockType.valueOf(s));
        }

        // handle @AccessTimeout
        attributes = MethodInfoUtil.resolveAttributes(accessTimeoutInfos, beanContext);

        if (log.isDebugEnabled()) {
            for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                final Method method = entry.getKey();
                final MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
                log.debug("AccessTimeout: " + method + " -- " + MethodInfoUtil.toString(value.methods.get(0)) + " " +
                    " " + value.accessTimeout.time + " " + value.accessTimeout.unit);
            }
        }

        for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
            final MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
            final MethodContext methodContext = beanContext.getMethodContext(entry.getKey());
            final Duration accessTimeout = new Duration(value.accessTimeout.time, TimeUnit.valueOf(value.accessTimeout.unit));
            methodContext.setAccessTimeout(accessTimeout);
        }
    }

    /**
     * This method splits the MethodConcurrencyInfo objects so that there is
     * exactly one MethodInfo per MethodConcurrencyInfo.  A single MethodConcurrencyInfo
     * with three MethodInfos would be expanded into three MethodConcurrencyInfo with
     * one MethodInfo each.
     *
     * The MethodConcurrencyInfo list is then sorted from least to most specific.
     *
     * @param infos
     * @return a normalized list of new MethodConcurrencyInfo objects
     */
    public static void normalize(final List<MethodConcurrencyInfo> infos,
                                 final List<MethodConcurrencyInfo> lockInfos,
                                 final List<MethodConcurrencyInfo> accessTimeoutInfos) {
        for (final MethodConcurrencyInfo oldInfo : infos) {
            for (final MethodInfo methodInfo : oldInfo.methods) {
                final MethodConcurrencyInfo newInfo = new MethodConcurrencyInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.concurrencyAttribute = oldInfo.concurrencyAttribute;
                newInfo.accessTimeout = oldInfo.accessTimeout;

                if (oldInfo.concurrencyAttribute != null) {
                    lockInfos.add(newInfo);
                }
                if (oldInfo.accessTimeout != null) {
                    accessTimeoutInfos.add(newInfo);
                }
            }
        }

        Collections.reverse(lockInfos);
        lockInfos.sort(new MethodConcurrencyComparator());

        Collections.reverse(accessTimeoutInfos);
        accessTimeoutInfos.sort(new MethodConcurrencyComparator());
    }

    public static class MethodConcurrencyComparator extends MethodInfoUtil.BaseComparator<MethodConcurrencyInfo> {
        public int compare(final MethodConcurrencyInfo a, final MethodConcurrencyInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }
}
