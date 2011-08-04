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
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.MethodContext;

import javax.ejb.LockType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodConcurrencyBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, MethodConcurrencyBuilder.class);

    public void build(HashMap<String, BeanContext> deployments, List<MethodConcurrencyInfo> methodConcurrencys) throws OpenEJBException {
        for (BeanContext beanContext : deployments.values()) {
            MethodConcurrencyBuilder.applyConcurrencyAttributes(beanContext, methodConcurrencys);
        }
    }

    public static void applyConcurrencyAttributes(BeanContext beanContext, List<MethodConcurrencyInfo> methodConcurrencyInfos) throws OpenEJBException {

        if (beanContext.isBeanManagedConcurrency()) return;

        Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), MethodConcurrencyBuilder.class);
        
        List<MethodConcurrencyInfo> lockInfos = new ArrayList<MethodConcurrencyInfo>();
        List<MethodConcurrencyInfo> accessTimeoutInfos = new ArrayList<MethodConcurrencyInfo>();
        
        MethodConcurrencyBuilder.normalize(methodConcurrencyInfos, lockInfos, accessTimeoutInfos);
        
        Map<Method, MethodAttributeInfo> attributes;
        
        // handle @Lock
        attributes = MethodInfoUtil.resolveAttributes(lockInfos, beanContext);
        
        if (log.isDebugEnabled()) {
            for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                Method method = entry.getKey();
                MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
                log.debug("Lock: " + method + " -- " + MethodInfoUtil.toString(value.methods.get(0)) + 
                          " " + value.concurrencyAttribute);
            }
        }

        for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
            MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
            MethodContext methodContext = beanContext.getMethodContext(entry.getKey());
            String s = value.concurrencyAttribute.toUpperCase();
            methodContext.setLockType(LockType.valueOf(s));
        }
        
        // handle @AccessTimeout
        attributes = MethodInfoUtil.resolveAttributes(accessTimeoutInfos, beanContext);
            
        if (log.isDebugEnabled()) {
            for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                Method method = entry.getKey();
                MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
                log.debug("AccessTimeout: " + method + " -- " + MethodInfoUtil.toString(value.methods.get(0)) + " " +
                		  " " + value.accessTimeout.time + " " + value.accessTimeout.unit);
            }
        }
        
        for (Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
            MethodConcurrencyInfo value = (MethodConcurrencyInfo) entry.getValue();
            MethodContext methodContext = beanContext.getMethodContext(entry.getKey());
            Duration accessTimeout = new Duration(value.accessTimeout.time, TimeUnit.valueOf(value.accessTimeout.unit));
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
    public static void normalize(List<MethodConcurrencyInfo> infos,
                                 List<MethodConcurrencyInfo> lockInfos,
                                 List<MethodConcurrencyInfo> accessTimeoutInfos) {
        for (MethodConcurrencyInfo oldInfo : infos) {
            for (MethodInfo methodInfo : oldInfo.methods) {
                MethodConcurrencyInfo newInfo = new MethodConcurrencyInfo();
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
        Collections.sort(lockInfos, new MethodConcurrencyBuilder.MethodConcurrencyComparator());

        Collections.reverse(accessTimeoutInfos);
        Collections.sort(accessTimeoutInfos, new MethodConcurrencyBuilder.MethodConcurrencyComparator());
    }

    public static class MethodConcurrencyComparator extends MethodInfoUtil.BaseComparator<MethodConcurrencyInfo> {
        public int compare(MethodConcurrencyInfo a, MethodConcurrencyInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }
}
